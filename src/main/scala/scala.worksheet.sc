import landAdministrator.LandOverlayPurpose
import landAdministrator.LandOverlay
import scala.collection.mutable
import org.apache.xmlgraphics.util.dijkstra
import org.apache.commons.math3.geometry.spherical.twod.Edge
import scalax.collection.GraphTraversal
import scalax.collection.State
import scalax.collection.mutable.GraphLike
/** 
* @note This class is in charge of generating data for the simulation engine
* The area are in ha
* It generates the following:
  - Number of farm, their size (in term of surface), type of crops + bio farms
   (defined using statistics about Switzerland's Canton/District)
  - The lands, and land overlays 
  - Basic suppliers TODO define more precisely  
  - The agglomerations: 
    "x = n inhabitants" We create villages(x < 2k), town (2k < x < 5k), small city (5k < x < 20k),
    medium city (20k < x < 50k), big city (50k > x)
    TODO comment distancer les villes ? inclure une dimension spatiale ? 
  - People based on these cities
  - Other entities than farms involved in food supply chain
* For the moment we do not have any spatial locality, so no ways to known which parcels are neighbors
* Each generated type of data is stored inside one excel file 
*/

import landAdministrator.CadastralParcel
import Owner._
import farmpackage.Farm
import Simulation.Person
import Simulation.Simulation
import Simulation.Sim

import breeze.stats.distributions

import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import scala.jdk.CollectionConverters._
 
val f = new File("/Users/douglasbouchet/Desktop/SwissFarmSimulation/src/main/data/statistical_data/canton_stats.xlsx")
val workbook = WorkbookFactory.create(f)
val sheet = workbook.getSheetAt(0)  

/** this will be used to assign a number of parcels to each farm 
 * They should be store as variable of the class generator cause used in some of its methods
*/
var nbFarmPerCanton, nbFarmMore30ha, nbFarmMore10Less30, nbFarmLess10: List[(String, Int)] = List()

/** this will be used to determine the land overlays */ 
var totalCropsArea:      List[(String, Int)] = List()
var totalWheatCropsArea: List[(String, Int)] = List()
var totalSurface:        List[(String, Int)] = List()
var population:          List[(String, Int)] = List()


for (i <- 1 to 27) {
  nbFarmPerCanton = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(1).toString().toDouble).toInt) :: nbFarmPerCanton
  nbFarmMore30ha = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(2).toString().toDouble).toInt) :: nbFarmMore30ha
  nbFarmMore10Less30 = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(3).toString().toDouble).toInt) :: nbFarmMore10Less30
  nbFarmLess10 = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(4).toString().toDouble).toInt) :: nbFarmLess10
  /** This numbers seems a bit low, check afterwards total use for agricultural purpose */
  totalCropsArea = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(6).toString().toDouble).toInt) :: totalCropsArea
  totalWheatCropsArea = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(10).toString().toDouble).toInt) :: totalWheatCropsArea
  totalSurface = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(14).toString().toDouble).toInt*100) :: totalSurface
  population = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(15).toString().toDouble).toInt) :: population
}

nbFarmPerCanton
nbFarmMore30ha
nbFarmMore10Less30
nbFarmLess10
population
totalCropsArea
totalSurface
/** Next we construct the parcels 
 * We differentiate 2 types of parcels, the agricultural ones, the other
 * agricultural parcels range from 2 to 10 ha, following gaussian distribution of mean 5, var TBD (rm external values)
 * (this was a quick guess, try to find more precise infos)
 * other ranges from 0.03 ha to 2 ha, gaussian distrib of mean 0.06, var TBD
 */ 

/** Generate random areas until a total area is reached, following a gaussian distribution 
 * If generated area outside [min,max], generates a new one
 * @param min: Double, the minimum value
 * @param max: Double, the maximum value
 * @param mean: Double, mean of the gaussian
 * @param variance: Double, variance of the gaussian
 * @param until: Double, the total area we want to achieve
*/
def generateRdmArea(min: Double, max: Double, mean: Double, variance: Double, until: Double): List[Double] = {
  var remainingArea = until
  val gaussianDist  = distributions.Gaussian(mean,variance)
  var sample: Double = 0
  var areas: List[Double] = List()
  
  while(remainingArea > 0){
    sample = BigDecimal(gaussianDist.sample()).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble 
    if(sample >= min && sample <= max){
      areas = sample :: areas
      remainingArea -= sample
    }
  }
  return areas
}

/** TODO add some statistics about each commune in switzerland 
 * generate parcels for each of this communes based on these statististics (to give id to parcels) */ 
def generateParcels(canton: String): (List[CadastralParcel],List[CadastralParcel]) = {
  val cropAreas: Double = totalCropsArea.filter(_._1 == canton).head._2
  val totalArea: Double = totalSurface.filter(_._1 == canton).head._2
  var agriculturalParcels, otherParcels: List[CadastralParcel] = List()
  generateRdmArea(2,10,5,2.4,cropAreas).foreach(area => {agriculturalParcels ::= new CadastralParcel(("TBD",0),new Owner, List(), area)})
  generateRdmArea(0.03,2,0.06,2.4,cropAreas).foreach(area => {otherParcels ::= new CadastralParcel(("TBD",0),new Owner, List(), area)})

  (agriculturalParcels,otherParcels)
}

/** Assign in a round robin manner an amount of parcels to a small, medium, big farm.
 * Repeat until there is no parcels remaining
 * For each farm: 
 *  Choose a number of ha as following:
 *    small farm: from 2 to 9 (Uniform)
 *    medium farm: from 10 to 29 (Uniform)
 *    big farm: from 30 to 60 (Uniform)
 *  Assign parcels until area reach the number of ha
 */ 
// def assignParcelsToFarms(canton: String, _parcels: List[CadastralParcel], s: Simulation): List[Farm] = {
//   var parcels : List[CadastralParcel] = _parcels
//   val nSmallFarms: Int = nbFarmLess10.filter(_._1 == canton).head._2
//   val nMedFarms:   Int = nbFarmMore10Less30.filter(_._1 == canton).head._2
//   val nBigFarms:   Int = nbFarmMore30ha.filter(_._1 == canton).head._2
//   val nFarms:      Int = nbFarmPerCanton.filter(_._1 == canton).head._2

//   var assignedSmallFarms:List[Farm] = List()
//   var assignedMedFarms  :List[Farm] = List()
//   var assignedBigFarms  :List[Farm] = List()

//   var sum: Double = 0.0
//   var area: Double = 0.0
//   var ended: Boolean = false

//   while(!parcels.isEmpty && (ended == false)){
//     if(assignedSmallFarms.length < nSmallFarms){
//       area = 2 + scala.util.Random.nextInt(7)
//       var farm = new packagefarm.Farm(s)
//       while(sum < area && !parcels.isEmpty){
//           farm.parcels ::= parcels.head
//           parcels = parcels.tail
//           sum = 0.0
//           farm.parcels.foreach(parcel => (sum += parcel.area))
//       }
//       assignedSmallFarms ::= farm
//     }
//     else if(assignedMedFarms.length < nMedFarms){
//       area = 10 + scala.util.Random.nextInt(20)
//       var farm = new Farm(s)
//       while(sum < area && !parcels.isEmpty){
//         farm.parcels ::= parcels.head
//         parcels = parcels.tail
//         sum = 0.0
//         farm.parcels.foreach(parcel => (sum += parcel.area))
//       }
//       assignedMedFarms ::= farm
//     }
//     else if(assignedBigFarms.length < nBigFarms){
//       area = 30 + scala.util.Random.nextInt(31)
//       var farm = new Farm(s)
//       while(sum < area && !parcels.isEmpty){
//         farm.parcels ::= parcels.head
//         parcels = parcels.tail
//         sum = 0.0
//         farm.parcels.foreach(parcel => (sum += parcel.area))
//       }
//       assignedBigFarms ::= farm
//     }
//     else {
//       ended = true
//     }
//     sum = 0.0 
//   }
//   val farms: List[Farm] = assignedSmallFarms ::: assignedMedFarms ::: assignedBigFarms
//   /** we reached the expected number of farm for the canton
//    * if some parcels remain, add them to some farm randomly */
//   if(!parcels.isEmpty){
//     parcels.foreach(parcel => (farms(scala.util.Random.nextInt(nFarms)).parcels ::= parcel))
//   }
//   scala.util.Random.shuffle(farms)
// }

val s = new Simulation
val farm0: Farm = new Farm(s)
val farm1: Farm = new Farm(s)
val farm2: Farm = new Farm(s)
val farms: List[Farm] = List(farm0,farm1,farm2)

val parcel0 = new CadastralParcel(("parcel0",1), new Owner,List(), 10)
val parcel1 = new CadastralParcel(("parcel1",1), new Owner,List(), 20)
val parcel2 = new CadastralParcel(("parcel2",1), new Owner,List(), 5)
val parcel3 = new CadastralParcel(("parcel3",1), new Owner,List(), 5)
val parcel4 = new CadastralParcel(("parcel4",1), new Owner,List(), 10)

val parcels = List(parcel0,parcel1,parcel2,parcel3,parcel4)

farm0.addParcels(parcels)

var nLandOverlays = scala.util.Random.nextInt(3)


var list = List(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17)
//var sliced = list.sliding(list.length/3, 3).toList
list.length /3 + 1
var sliced = list.grouped(list.length /3 + 1).toList
sliced(0)
sliced(1)
sliced(2)
sliced.map(list => {
  list.map(elem => (elem, 50.0))
})


var x = List[(Int, String)]((1,"a"),(2,"b"))
x.map(_._1).sum

x
//assignParcelsToFarms("Jura", generateParcels("Jura")._1)(0).parcel
/** next step is to create some land overlays
 * TODO see how to do 
 */

/** generate people for each canton */
def generatePeople(canton: String, sim: Simulation): List[Person] = {
  (for (i <- 1 to population.filter(_._1 == canton).head._2) yield new Person(sim, false)).toList
}  

import scalax.collection.generator._
import scalax.collection.Graph
import roadNetwork._


//val generator = new GraphGen[Node, EdgeRoad]

/** Next we generate the road network 
 * Idea: get data for generating national roads in switzerland 
 * Next generate as an Ldiagram growing from the highway the others roads 
*/

val networkClass = new RoadNetwork

val network: scalax.collection.mutable.Graph[Intersection, EdgeRoad] = networkClass.roadNetwork

val node0 = new Intersection("0")
val node1 = new Intersection("1")
val node2 = new Intersection("2")
val node3 = new Intersection("3")
network.add(node0)
network.add(node1)
network.add(node2)
network.add(node3)

val r01 = new EdgeRoad[Intersection](node0, node1, "road 01", 10, 10)
val r02 = new EdgeRoad[Intersection](node0, node2, "road 02", 10, 10)
val r03 = new EdgeRoad[Intersection](node0, node3, "road 03", 10, 10)
val r12 = new EdgeRoad[Intersection](node1, node2, "road 12", 20, 10)
val r13 = new EdgeRoad[Intersection](node1, node3, "road 13", 15, 10)


network.find(node0)

network.add(r01)
network.add(r02)
network.add(r03)
network.add(r12)
network.add(r13)

/** Should generate the nodes based on some stats about canton/communes 
 * The idea is to get some clusters representing the cities. 
 * and some intermediates nodes between the cities to make junction between them
 */
def generateNodes(n: Int): List[Intersection] = (for (i <- 0 to n-1) yield new Intersection(i.toString())).toList


/** add edges in a random way. If graph not connected until a certain amount of iteration, connect the remaining 
 * isolated nodes */
def generateEdges(roadNetworkInstance: RoadNetwork) = {
  var road = null
  val networkGraph: scalax.collection.mutable.Graph[Intersection, EdgeRoad] = roadNetworkInstance.roadNetwork
  val nNodes = networkGraph.nodes.length
  var nRoads: Int = 0
  while(!networkGraph.isConnected && nRoads < 100){
    val fromNode: Intersection = networkGraph.nodes.toList(scala.util.Random.nextInt(nNodes))
    val toNode: Intersection = networkGraph.nodes.toList(scala.util.Random.nextInt(nNodes))
    //check if node != 
    roadNetworkInstance.CreateRoad(fromNode, toNode,"name TBD", 10, 80, 35)
    nRoads += 1
  }
}

/** we make edges on the graph until it is connected, add random edge 
 * Then we create an empty Set. 
 * Create tuple in the form (Node, Int), the node and its key
 * Apply the algo
 * */  

//def minSpanningTree(graph: scalax.collection.mutable.Graph[Intersection, EdgeRoad]): scalax.collection.mutable.Graph[Intersection, EdgeRoad] = {
//  var addedNodes: List[Intersection] = List()
//  var remainingNodes: List[Intersection] = List()
//
//  graph.nodes.toList.foreach(node => addedNodes ::= node)
//  graph 
//}
//
//var addedNodes: List[Intersection] = List()
//var remainingNodes: List[Intersection] = List()
//var nodeKeys: List[(Intersection, Int)] = List()
//
//network.nodes.toList.foreach(node => remainingNodes ::= node)
//remainingNodes.foreach(node => nodeKeys ::= (node, Int.MaxValue))
//
//val startingNode = (nodeKeys(0)._1, 0)
//nodeKeys = nodeKeys.updated(0, startingNode)
//nodeKeys
///** Next we perform the Prim's algorithm to create a minimum spanning tree */
////while(!remainingNodes.isEmpty){
////  val maxKeys = nodeKeys.minBy(_._2)
////}
///** we select the node with lower key */
//var minKey: Intersection = nodeKeys.minBy(_._2)._1
///** we update keys of its adjency nodes */
//var candidates: Set[EdgeRoad[Intersection]]
//


//nodeKeys = nodeKeys.updated(minKey.incoming.head._2, nodeKeys.filter(tup => tup._1 == minKey.incoming.head._2))

//minKey.incoming.foreach(edge => {
//  nodeKeys = nodekeys.updated(edge._, nodeKeys.filter(tup => tup._1 == minKey.incoming.head._2))
//})



// generateEdges(networkClass)

/** We know the number of farms per canton. We assign them a random number of parcels of agricultural purpose */

/** How to assign some land overlays ? TBD */

//TODO erreur: prendre en compte le fait que c'est pas A REVOIR 


 //TODO ajouter le nombre d'emploi ? dispo dans les excels 


 //https://www.atlas.bfs.admin.ch/maps/13/fr/15467_75_3501_70/24217.html