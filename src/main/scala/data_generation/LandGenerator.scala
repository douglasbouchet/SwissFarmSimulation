import geography.CadastralParcel
import org.apache.poi.ss.usermodel.WorkbookFactory

import scala.annotation.tailrec
import java.io.File
import scala.collection.mutable


val rnd: scala.util.Random = new scala.util.Random // fix the seed
val f = new File("/Users/douglasbouchet/Desktop/SwissFarmSimulation/src/main/data/statistical_data/canton_stats.xlsx")
val sheet = WorkbookFactory.create(f).getSheetAt(0)

var totalWheatCropsArea: List[(String, Int)] = List()
var totalCropsArea:      List[(String, Int)] = List()
var totalSurface:        List[(String, Int)] = List()

var canton = "Suisse"

/** get data from excel file (26 cantons + Switzerland) */
for (i <- 1 to 27) {
  /** This numbers seems a bit low, check afterwards total use for agricultural purpose */
  totalCropsArea = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(6).toString().toDouble).toInt) :: totalCropsArea
  totalWheatCropsArea = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(
    10).toString().toDouble).toInt) :: totalWheatCropsArea
  totalSurface = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(14).toString().toDouble).toInt*100) :: totalSurface
}

val areaOfParcel: Int = 1
val sideLength: Int = math.sqrt(totalCropsArea.filter(_._1 == canton).head._2).toInt
val parcels = Array.ofDim[CadastralParcel](sideLength, sideLength)
val parcelsToIndex = scala.collection.mutable.Map[CadastralParcel, Int]()

/**
 *
 * @param canton
 * @return a tuple of (the new parcels
 */
def generateParcels(canton: String) = {
  val parcels = (for (_ <- 1 to totalCropsArea.filter(_._1 == canton).head._2 / areaOfParcel) yield new CadastralParcel(("Douglas", 1111), null, List(), 1)).toList
  parcels.zipWithIndex.foreach(t => parcelsToIndex.put(t._1, t._2))
  (parcels,parcelsToIndex)
}

def getNeighbors(parcel: CadastralParcel): List[CadastralParcel] = {
  List()
}

def getNeighborsInRange(parcel: CadastralParcel, radius: Int): List[CadastralParcel] = {
  List()
}

generateParcels("Suisse")