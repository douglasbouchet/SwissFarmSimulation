import landAdministrator.CadastralParcel
import Owner._
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
* Each generated type of data is stored inside one excel file 
*/

import breeze.stats.distributions

//class Generator {
//
//  //def getStastisticalDataFromExcel(excel: )
//}
import org.apache.poi.ss.usermodel.{ DataFormatter, WorkbookFactory, Row }
import java.io.File
import scala.jdk.CollectionConverters._
 


//def getStastisticalDataFromExcel()
val f = new File("/Users/douglasbouchet/Desktop/SwissFarmSimulation/src/main/data/statistical_data/canton_stats.xlsx")
val workbook = WorkbookFactory.create(f)
val sheet = workbook.getSheetAt(0)  

/** this will be used to assign a number of parcels to each farm 
 * They should be store as variable of the class generator cause used in some of its methods
*/
var nbFarmPerCanton, nbFarmMore30ha, nbFarmMore10Less30, nbFarmMoreLess10: List[(String, Int)] = List()

/** this will be used to determine the land overlays */ 
var totalCropsArea: List[(String, Int)] = List()
var totalWheatCropsArea: List[(String, Int)] = List()
var totalSurface: List[(String, Int)] = List()


for (i <- 1 to 27) {
  nbFarmPerCanton = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(1).toString().toDouble).toInt) :: nbFarmPerCanton
  nbFarmMore30ha = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(2).toString().toDouble).toInt) :: nbFarmMore30ha
  nbFarmMore10Less30 = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(3).toString().toDouble).toInt) :: nbFarmMore10Less30
  nbFarmMoreLess10 = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(4).toString().toDouble).toInt) :: nbFarmMoreLess10
  /** This numbers seems a bit low, check afterwards total use for agricultural purpose */
  totalCropsArea = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(6).toString().toDouble).toInt) :: totalCropsArea
  totalWheatCropsArea = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(10).toString().toDouble).toInt) :: totalWheatCropsArea
  totalSurface = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(14).toString().toDouble).toInt*100) :: totalWheatCropsArea
}

nbFarmPerCanton
nbFarmMore30ha
nbFarmMore10Less30
nbFarmMoreLess10 
totalCropsArea
totalWheatCropsArea
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

var x = List(2)
x ::= 3
x

def generateParcels(canton: String): (List[CadastralParcel],List[CadastralParcel]) = {
  val cropAreas: Double = totalCropsArea.filter(_._1 == canton).head._2
  val totalArea: Double = totalSurface.filter(_._1 == canton).head._2
  var agriculturalParcels, otherParcels: List[CadastralParcel] = List()
  generateRdmArea(2,10,5,2.4,cropAreas).foreach(area => {agriculturalParcels ::= new CadastralParcel(("TBD",0),new Owner, List(), area)})
  generateRdmArea(0.03,2,0.06,2.4,cropAreas).foreach(area => {otherParcels ::= new CadastralParcel(("TBD",0),new Owner, List(), area)})

  (agriculturalParcels,otherParcels)
}
generateRdmArea(0.03,2,0.06,2.4,1000000)

generateParcels("Bale-Ville")._1.length
generateParcels("Bale-Ville")._2.length
// TODO method


/** Next we create a number of farms, and assign them parcels of agricultural purpose in order to reach statistics about number of farms
 * and surface per farm (assign farms in order to have adjacent parcels) */

/** How to assign some land overlays ? TBD */



 //TODO ajouter le nombre d'emploi ? dispo dans les excels 


 //https://www.atlas.bfs.admin.ch/maps/13/fr/15467_75_3501_70/24217.html