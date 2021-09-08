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
import Simulation._
import landAdministrator.CadastralParcel
import Owner._
import farmpackage._
import Simulation.Person
import Simulation.Simulation

import Simulation.Factory._


import breeze.stats.distributions
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import scala.jdk.CollectionConverters._
import landAdministrator.LandOverlay
import landAdministrator.LandAdministrator
import landAdministrator.LandOverlayPurpose._
import Simulation.SimLib.Mill


class Generator {
 
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

  /** Next we construct the parcels 
   * We differentiate 2 types of parcels, the agricultural ones, the other
   * agricultural parcels range from 2 to 10 ha, following gaussian distribution of mean 5, var TBD (rm external values)
   * (this was a quick guess, try to find more precise infos)
   * other ranges from 0.03 ha to 2 ha, gaussian distrib of mean 0.06, var TBD
   * TODO add some statistics about each commune in switzerland 
   * generate parcels for each of this communes based on these statististics (to give id to parcels) */ 
  def generateParcels(canton: String): (List[CadastralParcel],List[CadastralParcel]) = {
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
  def assignParcelsToFarms(canton: String, _parcels: List[CadastralParcel], s: Simulation): List[Farm] = {
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
        var farm: Farm = new Farm(s)
        assignAreas(farm)
        assignedSmallFarms ::= farm
      }
      else if(assignedMedFarms.length < nMedFarms){
        area = 10 + scala.util.Random.nextInt(20)
        var farm = new Farm(s)
        assignAreas(farm)
        assignedMedFarms ::= farm
      }
      else if(assignedBigFarms.length < nBigFarms){
        area = 30 + scala.util.Random.nextInt(31)
        var farm = new Farm(s)
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
  def createAndAssignLandOverlays(farms: List[Farm], landAdministrator: LandAdministrator) = {
    farms.foreach{farm => {
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
          if (n < 75) {
            landAdministrator.purposeOfLandOverlay += (overlay -> wheatField)
            overlay.purpose = wheatField
          }
          //else if (n >= 75 && n < 95){
          else{
            landAdministrator.purposeOfLandOverlay += (overlay -> paddoc)
            overlay.purpose = paddoc
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

  /** Next we generate the road network */

  /** generate people for each canton */
  def generatePeople(canton: String, sim: Simulation): List[Person] = {
    (for (i <- 1 to population.filter(_._1 == canton).head._2) yield new Person(sim, false)).toList
  }  

  /** Generate Mills based on production of a canton
   *  The production if mills is a pure guess. Change it afterwards
   * @param canton
   */
  def generateMills(canton: String, s: Simulation): List[Mill] = {
    val cropAreas: Double = totalCropsArea.filter(_._1 == canton).head._2
    val tonnesOfWheat: Int = math.round((cropAreas*CONSTANTS.WHEAT_PRODUCED_PER_HA).toFloat)
    /** Assum Mill handle 100T of wheat per turn */
    val nMills = tonnesOfWheat/100000  
    //(for (i <- 1 to nMills) yield Mill(s)).toList
    (for (i <- 1 to 2) yield Mill(s)).toList
  }

  // TODO after
  //def generateSources()

  //https://www.atlas.bfs.admin.ch/maps/13/fr/15467_75_3501_70/24217.html
}


class Sauvegarde {

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

}