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
package generator 
import geography.{CadastralParcel, City, Coordinates, LandAdministrator, LandOverlay, LandOverlayPurpose, LocationAdministrator, PointF, RoadNetwork}
import Owner._
import farmpackage._
import _root_.Simulation.{Person, SimO, Simulation}
import farmrelated.cooperative.AgriculturalCooperative
import Securities.Commodities._
import breeze.stats.distributions
import org.apache.poi.ss.usermodel.WorkbookFactory

import java.io.File
import _root_.Simulation.SimLib.{Mill, Source}
import glob.Observator
import market.{ExternalCommodityDemand, Prices}

import scala.annotation.tailrec


class Generator {

  val rnd: scala.util.Random = new scala.util.Random // fix the seed
  val f = new File("/Users/douglasbouchet/Desktop/SwissFarmSimulation/src/main/data/statistical_data/canton_stats.xlsx")
  val sheet = WorkbookFactory.create(f).getSheetAt(0)  

  /** this will be used to assign a number of parcels to each farm 
   * They should be store as variable of the class generator cause used in some of its methods
  */
  var nbFarmPerCanton, nbFarmMore30ha, nbFarmMore10Less30, nbFarmLess10: List[(String, Int)] = List()
  var totalCropsArea:      List[(String, Int)] = List()
  var totalWheatCropsArea: List[(String, Int)] = List()
  var totalSurface:        List[(String, Int)] = List()
  var population:          List[(String, Int)] = List()

  /** get data from excel file (26 cantons + Switzerland) */
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


  /**
   * Start from a unique square of surface totalSurface, and split it until we reach parcels of some ha of area
   * @param totalSurface, in ha
   * @return the created CadastralParcels
   */
  /*def generateCadastralParcel(canton: String, maxSize: Double): List[CadastralParcel] = {
    /** Split each parcel into 4 smaller parcels until each parcel is at most maxSize
     * @param maxSize: if a parcel area is greater than maxSize(in ha), then it is split again
     * @note: This is really slow, to generate parcels of max size 0.5 ha, from 60000ha we need ~2 minutes (complexity in O(n^2))
     * */
    @tailrec
    def splitParcel(toSplit: CadastralParcel, remainingToSplit: List[CadastralParcel], acc: List[CadastralParcel], maxSize: Double): List[CadastralParcel] = {
      //
      if(toSplit == null){
        acc
      }
      else{
        val newParcels: List[CadastralParcel] = toSplit.splitCadastralParcel()
        val toBigParcels: List[CadastralParcel] = newParcels.filter(_.coordinates.computeArea() > maxSize)
        val correctParcels: List[CadastralParcel] = newParcels.diff(toBigParcels) // TODO check maybe do a filter not instead
        val newParcelsToSplit: List[CadastralParcel] = remainingToSplit ::: toBigParcels
        newParcelsToSplit.headOption match {
          case Some(parcel) => splitParcel(parcel, newParcelsToSplit.tail, acc ::: correctParcels, maxSize)
          case None => splitParcel(null, newParcelsToSplit, acc ::: correctParcels, maxSize)
        }
      }
    }

    //val totalArea = totalSurface.filter(_._1 == canton).head._2 * 10000 // in ha
    val totalArea = 60000 // in ha
    val sidelength: Double = math.sqrt(totalArea)
    val baseCoord = Coordinates(PointF(0,0),PointF(0,sidelength),PointF(sidelength,sidelength),PointF(sidelength,0))
    val initialParcel: CadastralParcel = new CadastralParcel(("Doug le bg", 1111), new Owner(), List(), baseCoord.computeArea(), baseCoord)

    splitParcel(initialParcel, List[CadastralParcel](), List[CadastralParcel](), maxSize)
  }

  */

  /**
   * Generate cities belonging to one canton, 1 districts and random location
   * @param nCities, the number of city we want to generate
   * @return the generated cities
   * @note only works with at most 4 cities at the moment (or find more city names)
   */
  @tailrec
  final def generateCities(nCities: Int, cites: List[City], names: List[String] = List[String]("Morges", "Saint-Sulpice", "Cossonay", "Lausanne")): List[City] = {
    if(nCities > 0){
      // class City(_name: String, _district: String, _canton: String, _centerCoord: (Double, Double))
      val name: String = names.head
      val coord: (Double, Double) = (45 + rnd.nextDouble()*2, 40 + rnd.nextDouble()) //make cities close to each other (as in real life for these cities names)
      generateCities(nCities - 1, cites :+ new City(name, "MORGES", "Vaud", coord), names.tail)
    }
    else cites
  }

  /** Generate random areas until a total area is reached, following a gaussian distribution 
   * If generated area outside [min,max], generates a new one
   * @param min: Double, the minimum value
   * @param max: Double, the maximum value
   * @param mean: Double, mean of the gaussian
   * @param variance: Double, variance of the gaussian
   * @param until: Double, the total area we want to achieve
  */
  private def generateRdmArea(min: Double, max: Double, mean: Double, variance: Double, until: Double): List[Double] = {
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
    areas
  }

  /** Next we construct the parcels 
   * We differentiate 2 types of parcels, the agricultural ones, the other
   * agricultural parcels range from 2 to 10 ha, following gaussian distribution of mean 5, var TBD (rm external values)
   * (this was a quick guess, try to find more precise infos)
   * other ranges from 0.03 ha to 2 ha, gaussian distrib of mean 0.06, var TBD
   * TODO add some statistics about each commune in switzerland 
   * generate parcels for each of this communes based on these statististics (to give id to parcels) */ 
  private def generateParcels(canton: String): (List[CadastralParcel],List[CadastralParcel]) = {
    val cropAreas: Double = totalCropsArea.filter(_._1 == canton).head._2
    val totalArea: Double = totalSurface.filter(_._1 == canton).head._2
    var agriculturalParcels, otherParcels: List[CadastralParcel] = List()
    generateRdmArea(2,10,5,2.4,cropAreas).foreach(area => {agriculturalParcels ::= new CadastralParcel(("TBD",0),new Owner, List(), area)})
    generateRdmArea(0.03,2,0.06,2.4,cropAreas).foreach(area => {otherParcels ::= new CadastralParcel(("TBD",0),new Owner, List(), area)})

    (agriculturalParcels,otherParcels)
  }

  /** Assign an amount of parcels to small, medium, big farms.
   * Repeat until number of farm of the canton is reached. Remaining parcels are distributed rdmly among existing farms.
   * For each farm: 
   *  Choose a number of ha as following:
   *    small farm: from 2 to 9 (Uniform)
   *    medium farm: from 10 to 29 (Uniform)
   *    big farm: from 30 to (60?) (Uniform)
   *  Assign parcels until area reach the number of ha
   */ 
  private def assignParcelsToFarms(canton: String, _parcels: List[CadastralParcel], s: Simulation, obs: Observator, prices: Prices): List[Farm] = {
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

    def assignAreas(_farm: Farm) {
    while(sum < area && !parcels.isEmpty){
          _farm.parcels ::= parcels.head
          parcels = parcels.tail
          sum = 0.0
          _farm.parcels.foreach(parcel => (sum += parcel.area))
      }
    }

    while(!parcels.isEmpty && (ended == false)){
      if(assignedSmallFarms.length < nSmallFarms){
        area = 2 + scala.util.Random.nextInt(7)
        var farm: Farm = new Farm(s, obs, prices)
        assignAreas(farm)
        assignedSmallFarms ::= farm
      }
      else if(assignedMedFarms.length < nMedFarms){
        area = 10 + scala.util.Random.nextInt(20)
        var farm = new Farm(s, obs, prices)
        assignAreas(farm)
        assignedMedFarms ::= farm
      }
      else if(assignedBigFarms.length < nBigFarms){
        area = 30 + scala.util.Random.nextInt(31)
        var farm = new Farm(s, obs, prices)
        assignAreas(farm)
        assignedBigFarms ::= farm
      }
      else {ended = true}
      sum = 0.0 
    }

    val farms: List[Farm] = assignedSmallFarms ::: assignedMedFarms ::: assignedBigFarms

    /** we reached the expected number of farm for the canton
     * if some parcels remain, add them to some farms randomly */
    if(!parcels.isEmpty){
      parcels.foreach(parcel => (farms(scala.util.Random.nextInt(nFarms)).parcels ::= parcel))
    }
    scala.util.Random.shuffle(farms)
  }

  // assignParcelsToFarms("Jura", generateParcels("Jura")._1)

  /** next step is to create some land overlays */

  /**
    * Create the land overlays for each farm (i.e which crops/paddoc/meadow it will have)
    * Select a number between 1 and 3 land overlays per farm (depending on number of parcels)
    * Assign each overlay between 1 parcel and 1/3 of total parcels of the farm
    * If the farm possess only 1 parcel, reduce the number of landOverlays
    * For each land overlay assign purpose:
    *   WheatField with proba 0.75
    *   Paddoc with proba 0.2
    *   Meadow with proba 0.05
    * @param farms: the farms on which we want to assign land overlays
    * @note farm is the only possessor of a land overlay. But this can change by time
    */
  private def createAndAssignLandOverlays(farms: List[Farm], landAdministrator: LandAdministrator) = {
    farms.foreach{farm => {
      println("Farm: ")
      val nParcels = farm.parcels.length
      var landOverlays: List[LandOverlay] = List()
      if(nParcels == 1){
        //Assign only one landOverlay over 50% of the parcel
        landOverlays ::= new LandOverlay(List((farm.parcels.head, 0.5)))
      }
      else if(nParcels == 2){
        landOverlays ::= new LandOverlay(List((farm.parcels(0), 0.7)))
        landOverlays ::= new LandOverlay(List((farm.parcels(1), 0.7)))
      }
      else {
        //Split the parcels into 3 groups. Each each parcel is assigned 70% of its area to land overlay
        var sliced0 = farm.parcels.slice(0,nParcels/3).toList
        var sliced1 = farm.parcels.slice(nParcels/3,2*nParcels/3).toList
        var sliced2 = farm.parcels.slice(2*nParcels/3,nParcels).toList
        var splittedParcels = List(sliced0,sliced1,sliced2).map(list => {
          list.map(elem => (elem, 0.7))
        })
        landOverlays ::= new LandOverlay(splittedParcels(0))
        landOverlays ::= new LandOverlay(splittedParcels(1))
        landOverlays ::= new LandOverlay(splittedParcels(2))
      }
      
      //randomly select a purpose to each landOverlay
      landOverlays.foreach {overlay => {
          landAdministrator.landOverlays ::= overlay
          val n = scala.util.Random.nextInt(100)
          if (n < 50) {
            println("Generating a wheat field")
            landOverlays = landOverlays.filterNot(_ == overlay)
            landOverlays ::= landAdministrator.changePurpose(overlay, LandOverlayPurpose.wheatField)
          }
          //else if (n >= 75 && n < 95){
          else{
            println("Generating a paddock")
            //remove the landOverlay and create a Paddock instead (inherits from landOverlay so no problem)
            landOverlays = landOverlays.filterNot(_ == overlay)
            landOverlays ::= landAdministrator.changePurpose(overlay, LandOverlayPurpose.paddock)
          }
          //else{
          //  landAdministrator.purposeOfLandOverlay += (overlay -> meadow)
          //  overlay.purpose = meadow
          //}
        }
      }
      farm.landOverlays :::= landOverlays
    }
    }
  }

  def generateSources(canton: String, s: Simulation): List[Source] = {
    val seedsSeller = new Source(WheatSeeds, 10000000,300, s);
    val seedsSeller1 = new Source(WheatSeeds, 100000,340, s);
    val feedStuffSeller = new Source(FeedStuff, 100000,100, s);
    val grassSource = new Source(Grass, units = 1000000, 100, s)
    List(seedsSeller, seedsSeller1, feedStuffSeller, grassSource)
  }

  /** Next we generate the road network */

//This part contains method to generate the farms/mills/people of the simulation


/**Create people living in a canton, and push them on labour_market (i.e they can be hire) */
private def initPerson(canton: String, s: Simulation): List[Person] = {
  val people = (for (i <- 1 to population.filter(_._1 == canton).head._2) yield new Person(s, false)).toList
  println("Generating " + people.length + " people")
  //s.labour_market.pushAll(people)
  people
  //sims ++= people
}

private def initLandsAndFarms(canton: String, landAdministrator: LandAdministrator, s: Simulation, obs: Observator, prices: Prices): List[Farm] = {
  //Init generate parcels, and assign them to farms
  val allParcels = generateParcels(canton)
  landAdministrator.cadastralParcels = allParcels._1 ::: allParcels._2
  //var farms = generator.assignParcelsToFarms(canton, allParcels._1, this)
  val farms = assignParcelsToFarms(canton, allParcels._1, s, obs, prices).take(2)
  println(farms.length + " farms created ")
  createAndAssignLandOverlays(farms, landAdministrator)
  farms.foreach(_.init)

  farms
}
/** Generate Mills based on production of a canton
 *  The production if mills is a pure guess. Change it afterwards
 * @param canton
 */
 private def initMills(canton: String, s: Simulation): List[Mill] = {
  val cropAreas: Double = totalCropsArea.filter(_._1 == canton).head._2
  val tonnesOfWheat: Int = math.round((cropAreas*CONSTANTS.WHEAT_PRODUCED_PER_HA).toFloat)
  /** Assum Mill handle 100T of wheat per turn */
  val nMills = tonnesOfWheat/100000  
  //(for (i <- 1 to nMills) yield Mill(s)).toList
  //(for (i <- 1 to 2) yield new Mill(s)).toList
  List[Mill]()

}

/** Create some cooperatives, and assign them farms
  * @param canton Useless for the moment, but used to create the cooperative when we get the data
  * @param farms
  * @param s
  * @return a list of AgriculturalCooperative
  */
private def initCoop(canton: String, farms: List[Farm], s: Simulation): List[AgriculturalCooperative] = {
  //for the moment, only create 1 coop
  List[AgriculturalCooperative](new AgriculturalCooperative(farms, List(Wheat, Fertilizer), s))
}

  /** Add near agents inside contact network of each owner
   * @param owners the owners whom we want to add people to their contact network
   * @param radius: each agent present inside this radius from an owner's radius could be added inside its contact network (do not add all but some)
   */
  /*private def initRelations(owners: List[Owner], radius: Double): Unit = {

  owners.foreach{
    //If instance of Farm, we should add one source, and
    case owner@(_: Farm) => {}
  }
  }*/

/** Generate {lands, farms, mills, people, agricultural cooperative} for a given canton
 * Also generate cities where farms and mills are placed
 * @param landAdministrator: this function create its parcels and land overlays
 * @return a list of all agents, that should be put as argument into init function of simulation
 * */
def generateAgents(canton: String, landAdministrator: LandAdministrator, s: Simulation): Unit = {

  //val parcels = generateCadastralParcel(canton, 2)
  val observator: Observator = new Observator(s, List())
  val prices: Prices = new Prices(s)
  val externalCommodityDemand: ExternalCommodityDemand = new ExternalCommodityDemand(s, observator)
  val nCities = 4
  val cities: List[City] = generateCities(nCities, List())
  LocationAdministrator.init(cities)
  val people: List[Person] = initPerson(canton, s)
  s.init(people)
  val farms: List[Farm] = initLandsAndFarms(canton, landAdministrator, s, observator, prices).take(2)
  val mills: List[Mill] = initMills(canton, s)
  val coop : List[AgriculturalCooperative] = initCoop(canton, farms, s)
  val sources: List[Source] = generateSources(canton, s)

  observator.farms :::= farms


  //we place farms and cooperative inside cities

  coop.foreach(_.city = cities(rnd.nextInt(nCities)))

  s.init(List(observator, prices, externalCommodityDemand) ::: farms ::: mills ::: coop ::: sources)

  generateRoadNetwork()
}

def generateRoadNetwork(): RoadNetwork = {
  val roadNetworkInstance: RoadNetwork = new RoadNetwork()
  LocationAdministrator.cities.forall(city => roadNetworkInstance.createNode(city.name) == true)
  LocationAdministrator.cities.foreach((cityA: City) => {
    LocationAdministrator.cities.filterNot(_ == cityA).foreach((cityB:City) => {
      //we add an edge of fly distance length between CityA and all CityB
      roadNetworkInstance.createRoad(cityA, cityB, cityA.name + " to " + cityB.name,
        LocationAdministrator.computeDistanceBetweenCities(cityA, cityB),80,35)
    })
  })
 // roadNetworkInstance.createRoad(LocationAdministrator.cities(0), LocationAdministrator.cities(1), "aaa", 80,90,90)
  //Next we add a road between each nodes (fully connected atm, but TODO this will change when generating a more complex network)

  //val path = roadNetworkInstance.findPath(LocationAdministrator.cities(0),LocationAdministrator.cities(1)).nodes
  //val path2 = roadNetworkInstance.findPath(LocationAdministrator.cities(0),LocationAdministrator.cities(1)).length
  //val path3 = roadNetworkInstance.findPath(LocationAdministrator.cities(0),LocationAdministrator.cities(1)).weight
  roadNetworkInstance
}

  //def generateSources()

  //https://www.atlas.bfs.admin.ch/maps/13/fr/15467_75_3501_70/24217.html
}


//class Sauvegarde {

  // Should use an SQL database instead of raw json/csv files, in order to target the changes ? 

  // Supppose we have a new type of farms, we can just get the only necessary data to change 
  // maybe do not save entires object but rather infos on them 

  // advantages of Json, may be easier if for example we want to model the fact that each farm can have its own network.
  // can we do such a thing in sql ? 


  /** Possibilities:
   * Save states of objects inside Json files
   * Save on an SQL database */

   // we may want to be able to quicly retrieve some objects 

   // Do we need to have an organization for objects storing ? 

//}