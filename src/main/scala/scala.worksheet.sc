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


class Generator {

  //def getStastisticalDataFromExcel(excel: )
}
import org.apache.poi.ss.usermodel.{ DataFormatter, WorkbookFactory, Row }
import java.io.File
import scala.jdk.CollectionConverters._
 


//def getStastisticalDataFromExcel()
val f = new File("/Users/douglasbouchet/Desktop/SwissFarmSimulation/src/main/data/statistical_data/canton_stats.xlsx")
val workbook = WorkbookFactory.create(f)
val sheet = workbook.getSheetAt(0)  

/** this will be used to assign a number of parcels to each farm */
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
 * We assume that agricultural parcels range from 2 ha to 10 ha, following gaussian distrib center in 5 (rm external values)
 * (this was a quick guess, try to find more precise infos)
 * 
 * */ 



 //TODO ajouter le nombre d'emploi ? dispo dans les excels 


 //https://www.atlas.bfs.admin.ch/maps/13/fr/15467_75_3501_70/24217.html