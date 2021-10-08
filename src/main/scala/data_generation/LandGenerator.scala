package data_generation

import org.apache.poi.ss.usermodel.WorkbookFactory
import scala.annotation.tailrec
import java.io.File

class LandGenerator {

  val rnd: scala.util.Random = new scala.util.Random // fix the seed
  val f = new File("/Users/douglasbouchet/Desktop/SwissFarmSimulation/src/main/data/statistical_data/canton_stats.xlsx")
  val sheet = WorkbookFactory.create(f).getSheetAt(0)

  var totalWheatCropsArea: List[(String, Int)] = List()
  var totalCropsArea:      List[(String, Int)] = List()
  var totalSurface:        List[(String, Int)] = List()

  /** get data from excel file (26 cantons + Switzerland) */
  for (i <- 1 to 27) {
    /** This numbers seems a bit low, check afterwards total use for agricultural purpose */
    totalCropsArea = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(6).toString().toDouble).toInt) :: totalCropsArea
    totalWheatCropsArea = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(10).toString().toDouble).toInt) :: totalWheatCropsArea
    totalSurface = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(14).toString().toDouble).toInt*100) :: totalSurface
  }
}
