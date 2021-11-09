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
package generation

import Companies.{Mill, Supermarket}
import Government.Government
import Securities.Commodities.{Bread, Commodity, FeedStuff, Fertilizer, Flour, Grass, RapeseedSeeds, Soybeans, SoybeansSeeds, Wheat, WheatSeeds}
import _root_.Simulation.SimLib.Source
import modifyFromKoch.Trader
import _root_.Simulation.{SimO, Simulation}
import geography.LandOverlayPurpose.{LandOverlayPurpose, rapeseedField, soybeansField}
//import _root_.Simulation.Factory.ProductionLineSpec
import FarmRelated._
import geography._
import glob.Observator
import market.{ExternalCommodityDemand, Prices}
import modifyFromKoch.{Person, ProductionLineSpec}
import org.apache.poi.ss.usermodel.WorkbookFactory

import java.io.File
import scala.annotation.tailrec


class Generator(canton: String) {

  val rnd: scala.util.Random = new scala.util.Random // fix the seed
  //private val f = new File("C:/Users/youss/Desktop/SwissFarmSimulation/src/main/data/statistical_data/canton_stats.xlsx")
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
    nbFarmPerCanton = (sheet.getRow(i).getCell(0).toString, math.round(sheet.getRow(i).getCell(1).toString.toDouble).toInt) :: nbFarmPerCanton
    nbFarmMore30ha = (sheet.getRow(i).getCell(0).toString, math.round(sheet.getRow(i).getCell(2).toString.toDouble).toInt) :: nbFarmMore30ha
    nbFarmMore10Less30 = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(3).toString().toDouble).toInt) :: nbFarmMore10Less30
    nbFarmLess10 = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(4).toString().toDouble).toInt) :: nbFarmLess10
    /** This numbers seems a bit low, check afterwards total use for agricultural purpose */
    totalCropsArea = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(6).toString().toDouble).toInt) :: totalCropsArea
    totalWheatCropsArea = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(10).toString().toDouble).toInt) :: totalWheatCropsArea
    totalSurface = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(14).toString().toDouble).toInt*100) :: totalSurface
    population = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(15).toString().toDouble).toInt) :: population
  }


  /**
   * Generate cities belonging to one canton, 1 districts and random location
   * @param nCities, the number of city we want to generate
   * @return the generated cities
   * @note only works with at most 4 cities at the moment (or find more city names)
   */
  @tailrec
  final def generateCities(nCities: Int, cites: List[City], names: List[String] = List[String]("Morges", "Saint-Sulpice", "Cossonay", "Lausanne")): List[City] = {
    if(nCities > 0){
      // class City(_name: String, _district: String, _ _centerCoord: (Double, Double))
      val name: String = names.head
      val coord: (Double, Double) = (45 + rnd.nextDouble()*2, 40 + rnd.nextDouble()) //make cities close to each other (as in real life for these cities names)
      generateCities(nCities - 1, cites :+ new City(name, "MORGES", "Vaud", coord), names.tail)
    }
    else cites
  }

  /**
   * this will be used to randomly assign the companies all around the country
   * it modify the parcel's attribut of the company, thus nothing to return
   * @param company
   * @param parcels A list of remaining parcels (ownerless parcels)
   */


  /** This method is in charge of creating all the companies involved in the food supply chain
   * will generate the companies according to the canton statistics base crop/herd production
   * This also assign parcels to companies
   * TODO tomorrow
   * */
  private def CreateMills(s: Simulation, lAdmin: LandAdministrator, obs: Observator, canton: String): List[Mill] = {
    //we use the estimated total area used for crop purpose to deduce how much company we will need for the food supply chain
    val anticipatedWheatProd = totalWheatCropsArea.filter(_._1 == canton).head._2 * CONSTANTS.WHEAT_PRODUCED_PER_HA
    var remainingParcels: List[CadastralParcel] = rnd.shuffle(lAdmin.getFreeParcels)
    //from https://www.dsm-fms.ch/fr/donnees/chiffres/moulins/ -> 41 milling Companies
    val test = remainingParcels.filter(_.owner == null)
    // !!! for all switzerland, but let's do this atm
    //val nMills = 41
    val nMills = 1 //to test
    var mills = List[Mill]()
    //same for every mill, change it afterwards
    val basicPls = ProductionLineSpec(3,
                                      List(),
                                      List((Wheat, (anticipatedWheatProd/nMills).toInt)),
                                      (Flour, (anticipatedWheatProd/nMills *CONSTANTS.CONVERSION_WHEAT_FLOUR).toInt),
                                      CONSTANTS.FLOUR_PROD_DURATION)
    for (_ <- 1 to nMills){
        val parcel = remainingParcels.head
        require(parcel.owner == null)
        remainingParcels = remainingParcels.tail
        mills ::= Mill(s, lAdmin, List(parcel), basicPls)
    }
    println("Number of mills = " + mills.length)
    mills
  }

  //assume for the moment supermarket make their own bread (add bakery afterwards)
  //And only by wheat and soja atm, and does not by any rape
  private def CreateSupermarket(s: Simulation, lAdmin: LandAdministrator, obs: Observator, canton: String): List[Supermarket] = {
    //we use the estimated total area used for crop purpose to deduce how much company we will need for the food supply chain
    val anticipatedFlourProd = totalWheatCropsArea.filter(_._1 == canton).head._2 * CONSTANTS.WHEAT_PRODUCED_PER_HA* CONSTANTS.CONVERSION_WHEAT_FLOUR
    var remainingParcels: List[CadastralParcel] = rnd.shuffle(lAdmin.getFreeParcels)
    //val nSupermarket = 30 //Totally random
    val nSupermarket = 1 //To test
    var supermarkets = List[Supermarket]()
    //same for every supermarket, change it afterwards
    val basicPls = ProductionLineSpec(3,
                                      List(),
                                      List((Flour, (anticipatedFlourProd/nSupermarket).toInt)),
                                      (Bread, (anticipatedFlourProd/(nSupermarket * CONSTANTS.KG_FLOUR_FOR_1_BREAD)).toInt),
                                      10 * CONSTANTS.TICKS_TIMER_PER_DAY)
    for (_ <- 1 to nSupermarket){
      val parcel = remainingParcels.head
      require(parcel.owner == null)
      remainingParcels = remainingParcels.tail
      supermarkets ::= Supermarket(s, lAdmin, List(parcel), basicPls)
    }
    println("Number of supermarkets = " + supermarkets.length)
    supermarkets
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
  private def assignParcelsToFarms(s: Simulation, obs: Observator, prices: Prices, landAdmin: LandAdministrator): List[Farmer] = {
    var parcels : List[CadastralParcel] = landAdmin.cadastralParcels
    val nSmallFarms: Int = nbFarmLess10.filter(_._1 == canton).head._2
    val nMedFarms:   Int = nbFarmMore10Less30.filter(_._1 == canton).head._2
    val nBigFarms:   Int = nbFarmMore30ha.filter(_._1 == canton).head._2

    var assignedSmallFarms:List[Farmer] = List()
    var assignedMedFarms  :List[Farmer] = List()
    var assignedBigFarms  :List[Farmer] = List()

    var sum: Double = 0.0
    var area: Double = 0.0
    var ended: Boolean = false

    def assignAreas(_farm: Farmer): Unit = {
      //at least 3 parcels per farm
      while((sum < area && parcels.nonEmpty) || _farm.parcels.length < 6){
          _farm.parcels ::= parcels.head
          parcels = parcels.tail
          sum = 0.0
          _farm.parcels.foreach(parcel => {
            sum += parcel.area
            parcel.owner = _farm
          })
      }
    }

    while(!parcels.isEmpty && !ended){
      //TODO assign an age and a child to a farmer

      //Here we assign a child to a farmer with probability 0.875 and give an age according to
      //distribution of figure 3.3 of Swissland report (p. 21)
      var children = List[Child]()
      for (_ <- 0 to rnd.nextInt(3)) {
        if(rnd.nextFloat() < 0.5){
          //only male can work, and what to take over with proba 80%
          children ::= new Child(s: Simulation, 30, "male", rnd.nextFloat() < 0.8)
        }
      }
      var age = 0
      //val n = rnd.nextFloat()
      //if (n < 0.038) age = 22
      //else if(n < 0.137)  age = 66
      //else if(n < 0.797)  age = 30
      //else if(n < 0.3787) age = 40
      //else if(n < 0.7937) age = 50
      //else age = 64
      age = 60 // just to test they all exit


      if(assignedSmallFarms.length < nSmallFarms){
        area = 2 + scala.util.Random.nextInt(7)
        val farm: Farmer = new Farmer(s, obs,landAdmin, age, children)
        assignAreas(farm)
        assignedSmallFarms ::= farm
      }
      else if(assignedMedFarms.length < nMedFarms){
        area = 10 + scala.util.Random.nextInt(20)
        val farm = new Farmer(s, obs,landAdmin, age, children)
        assignAreas(farm)
        assignedMedFarms ::= farm
      }
      else if(assignedBigFarms.length < nBigFarms){
        area = 30 + scala.util.Random.nextInt(31)
        val farm = new Farmer(s, obs,landAdmin, age, children)
        assignAreas(farm)
        assignedBigFarms ::= farm
      }
      else {ended = true}
      sum = 0.0 
    }

   assignedSmallFarms ::: assignedMedFarms ::: assignedBigFarms

    /** we reached the expected number of farm for the canton
     * if some parcels remain, add them to some farms randomly */
    /** Currently not anymore, cause giving lands for both agricultural purpose and other purpose,
     * remaining parcels will be used by other agents */
    //if(!parcels.isEmpty){
    //  parcels.foreach(parcel => (farms(scala.util.Random.nextInt(nFarms)).parcels ::= parcel))
    //}
    //scala.util.Random.shuffle(farms)
  }

  // assignParcelsToFarms("Jura", generateParcels("Jura")._1)

  /** next step is to create some land overlays */

  /**
    * Create the land overlays for each farm (i.e which crops/paddoc/meadow it will have)
    * If #parcels in [2,10] (WheatField, and Paddock, noPurpose)
    * Else (WheatField, soybeansField, rapeseedField, Paddock, noPurpose)
    * If the farm possess only 1 parcel, reduce the number of landOverlays
    * For each land overlay assign purpose:
    *   WheatField with proba 0.75
    *   Paddoc with proba 0.2
    *   Meadow with proba 0.05
    * @param farms: the farms on which we want to assign land overlays
    * @note farm is the only possessor of a land overlay. But this can change by time
    */
  private def createAndAssignLandOverlays(farms: List[Farmer], landAdministrator: LandAdministrator) = {
    farms.foreach{farm => {
      val nParcels = farm.parcels.length
      var landOverlays: List[LandOverlay] = List()
      var toAssign: List[LandOverlayPurpose] = List[LandOverlayPurpose](LandOverlayPurpose.wheatField, LandOverlayPurpose.paddock, LandOverlayPurpose.noPurpose)
      if(nParcels > 5){
        print("we should have pb")
        toAssign :::= List(LandOverlayPurpose.soybeansField, LandOverlayPurpose.rapeseedField)
      }

      val nPurpose = toAssign.length //+1 for the no purpose LOverlay
      val parcelsRepartition : List[List[CadastralParcel]] = {
        for(i <- 1 to nPurpose) yield farm.parcels.slice((i-1)*nParcels/nPurpose,(i)*nParcels/nPurpose)
      }.toList
      //for each parcel, we give 90% of its area to the landOverlay
      val repartition: List[List[(CadastralParcel, Double)]] = parcelsRepartition.map(list => list.map((_, 0.9)))
      repartition.foreach(landOverlays ::= new LandOverlay(_))
      landAdministrator.landOverlays :::= landOverlays
      for(i <- 1 to nPurpose){
        landAdministrator.changePurpose(landOverlays(i-1), toAssign(i-1))
      }

      farm.landOverlays :::= landOverlays

      val x = 1


      /*if(nParcels == 1){
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
      */
      //assign in priority
      /*val len = landOverlays.length
      if(len >= 1){
        landAdministrator.changePurpose(landOverlays(0), LandOverlayPurpose.wheatField)
        if(len >= 2){
          landAdministrator.changePurpose(landOverlays(1), LandOverlayPurpose.paddock)
          if (len >= 3){
            landAdministrator.changePurpose(landOverlays(2), LandOverlayPurpose.soybeansField)
            if (len >= 4){
            landAdministrator.changePurpose(landOverlays(3), LandOverlayPurpose.rapeseedField)
            }
          }
        }
      }
      */


    }
    }
  }

  def generateSources( s: Simulation): List[Source] = {
    val seedsSeller = new Source(WheatSeeds, 10000000,300, s);
    val seedsSeller1 = new Source(WheatSeeds, 100000,340, s);
    val seedsSeller2 = new Source(SoybeansSeeds, 100000,340, s);
    val seedsSeller3 = new Source(RapeseedSeeds, 100000,340, s);
    val feedStuffSeller = new Source(FeedStuff, 100000,100, s);
    val grassSource = new Source(Grass, units = 1000000, 100, s)
    List(seedsSeller, seedsSeller1,seedsSeller2,seedsSeller3,  feedStuffSeller, grassSource)
  }

  /** Next we generate the road network */

//This part contains method to generate the farms/mills/people of the simulation


/**Create people living in a canton, and push them on labour_market (i.e they can be hire)
 * Assign parcels randomly atm, but should be could to create "Clusters to represent city, in the end"
 * We put groups of 4 people on the same parcel
 * */
private def initPerson(s: Simulation,lAdmin: LandAdministrator): List[Person] = {
  @tailrec
  def recCreatePerson(parcels: List[CadastralParcel], n: Int, acc: List[Person]): List[Person] = {
    if (n == 0) acc
    else {
      require(parcels.nonEmpty)
      val parcel = parcels.head
      recCreatePerson(parcels.tail, n - math.min(n, 4), acc ::: (for (_ <- 1 to math.min(n, 4)) yield new Person(s, true, parcel)).toList)
    }
  }
  //val nPeople = population.filter(_._1 == canton).head._2
  val nPeople = 1000
  println("Population = " + nPeople)
  val people = recCreatePerson(rnd.shuffle(lAdmin.getFreeParcels), nPeople, List[Person]())
  s.labour_market.pushAll(people)
  people
}

private def initLandsAndFarms(landAdministrator: LandAdministrator, s: Simulation, obs: Observator, prices: Prices): List[Farmer] = {

  //val farms = assignParcelsToFarms(s, obs, prices,landAdministrator)
  val farms = assignParcelsToFarms(s, obs, prices,landAdministrator).take(2)
  println("Number of farms = " + farms.length)
  createAndAssignLandOverlays(farms, landAdministrator)
  farms.foreach(_.init())
  obs.farms :::= farms
  farms
}

/** Create some cooperatives, and assign them farms
  * @param canton Useless for the moment, but used to create the cooperative when we get the data
  * @param farms
  * @param s
  * @return a list of AgriculturalCooperative
  */
private def initCoop(farms: List[Farmer], s: Simulation): List[AgriculturalCooperative] = {
  //for the moment, only create 1 coop
  List[AgriculturalCooperative](new AgriculturalCooperative(farms, List(Wheat, Fertilizer), s))
}

  /** Add near agents inside contact network of each owner
   * @param owners the owners whom we want to add people to their contact network
   * @param radius: each agent present inside this radius from an owner's radius could be added inside its contact network (do not add all but some)
   */
  /*private def initRelations(owners: List[Owner], radius: Double): Unit = {

  owners.foreach{
    //If instance of Farmer, we should add one source, and
    case owner@(_: Farmer) => {}
  }
  }*/

/** Generate {lands, farms, mills, people, agricultural cooperative} for a given canton
 * Also generate cities where farms and mills are placed
 * @param landAdministrator: this function create its parcels and land overlays
 * @return a list of all agents, that should be put as argument into init function of simulation
 * */
def generateAgents(landAdministrator: LandAdministrator, s: Simulation): Unit = {

  //val parcels = generateCadastralParcel(canton, 2)
  val government: Government = new Government(s)
  val observator: Observator = new Observator(s, List())
  val prices: Prices = new Prices(s)
  val externalCommodityDemand: ExternalCommodityDemand = new ExternalCommodityDemand(s, observator)
  val nCities = 4
  val cities: List[City] = generateCities(nCities, List())
  LocationAdministrator.init(cities)
  val people: List[Person] = initPerson(s, landAdministrator)
  val farms: List[Farmer] = initLandsAndFarms(landAdministrator, s, observator, prices)
  val mills: List[Mill] = CreateMills(s, landAdministrator, observator, canton)
  val supermarkets: List[Supermarket] = CreateSupermarket(s, landAdministrator, observator, canton)
  //val coop : List[AgriculturalCooperative] = initCoop(farms, s)
  val sources: List[Source] = generateSources(s)

  val sojaTrader = Trader(Soybeans, 40000, s)




  //we place farms and cooperative inside cities

  //coop.foreach(_.city = cities(rnd.nextInt(nCities)))

  s.init(people ::: List(government, observator, prices, externalCommodityDemand, sojaTrader) ::: farms /*::: coop*/ ::: mills ::: supermarkets ::: sources)

  //generateRoadNetwork()
}

def generateRoadNetwork(): RoadNetwork = {
  val roadNetworkInstance: RoadNetwork = new RoadNetwork()
  LocationAdministrator.cities.forall(city => roadNetworkInstance.createNode(city.name))
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