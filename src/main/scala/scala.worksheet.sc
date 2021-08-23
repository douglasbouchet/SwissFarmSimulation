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
import farmpackage._
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
def assignParcelsToFarms(canton: String, _parcels: List[CadastralParcel]): List[Farm] = {
  var parcels : List[CadastralParcel] = _parcels
  val nSmallFarms: Int = nbFarmLess10.filter(_._1 == canton).head._2
  val nMedFarms:   Int = nbFarmMore10Less30.filter(_._1 == canton).head._2
  val nBigFarms:   Int = nbFarmMore30ha.filter(_._1 == canton).head._2
  val nFarms:      Int = nbFarmPerCanton.filter(_._1 == canton).head._2

  var assignedSmallFarms:List[Farm] = List()
  var assignedMedFarms  :List[Farm] = List()
  var assignedBigFarms  :List[Farm] = List()

  var sum: Double = 0.0
  var area: Double = 0.0
  var ended: Boolean = false

  while(!parcels.isEmpty && (ended == false)){
    if(assignedSmallFarms.length < nSmallFarms){
      area = 2 + scala.util.Random.nextInt(7)
      var farm = new Farm
      while(sum < area && !parcels.isEmpty){
          farm.parcels ::= parcels.head
          parcels = parcels.tail
          sum = 0.0
          farm.parcels.foreach(parcel => (sum += parcel.area))
      }
      assignedSmallFarms ::= farm
    }
    else if(assignedMedFarms.length < nMedFarms){
      area = 10 + scala.util.Random.nextInt(20)
      var farm = new Farm
      while(sum < area && !parcels.isEmpty){
        farm.parcels ::= parcels.head
        parcels = parcels.tail
        sum = 0.0
        farm.parcels.foreach(parcel => (sum += parcel.area))
      }
      assignedMedFarms ::= farm
    }
    else if(assignedBigFarms.length < nBigFarms){
      area = 30 + scala.util.Random.nextInt(31)
      var farm = new Farm
      while(sum < area && !parcels.isEmpty){
        farm.parcels ::= parcels.head
        parcels = parcels.tail
        sum = 0.0
        farm.parcels.foreach(parcel => (sum += parcel.area))
      }
      assignedBigFarms ::= farm
    }
    else {
      ended = true
    }
    sum = 0.0 
  }
  val farms: List[Farm] = assignedSmallFarms ::: assignedMedFarms ::: assignedBigFarms
  /** we reached the expected number of farm for the canton
   * if some parcels remain, add them to some farm randomly */
  if(!parcels.isEmpty){
    parcels.foreach(parcel => (farms(scala.util.Random.nextInt(nFarms)).parcels ::= parcel))
  }
  scala.util.Random.shuffle(farms)
}

// assignParcelsToFarms("Jura", generateParcels("Jura")._1)

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

val network = new RoadNetwork

network.getEdges

/** Should generate the nodes based on some stats about canton/communes 
 */
def generateNodes(number: Int) = {

}

/** If nodes are between the same communes, give small length, otw give bigger size 
 * The graph should be connected. 
 * 
*/

def generateEdges(nodes: List[Node]) = {

}


/** We know the number of farms per canton. We assign them a random number of parcels of agricultural purpose */

/** How to assign some land overlays ? TBD */

//TODO erreur: prendre en compte le fait que c'est pas A REVOIR 


 //TODO ajouter le nombre d'emploi ? dispo dans les excels 


 //https://www.atlas.bfs.admin.ch/maps/13/fr/15467_75_3501_70/24217.html