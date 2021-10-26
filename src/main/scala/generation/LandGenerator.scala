package generation

import geography.CadastralParcel
import org.apache.poi.ss.usermodel.WorkbookFactory

import java.io.File

class LandGenerator() {
  
  private val rnd: scala.util.Random = new scala.util.Random // fix the seed
  //private val f = new File("D:/BA5/Projet_de_semestre/SwissFarmSimulation/src/main/data/statistical_data/canton_stats.xlsx")
  val f = new File("/Users/douglasbouchet/Desktop/SwissFarmSimulation/src/main/data/statistical_data/canton_stats.xlsx")
  private val sheet = WorkbookFactory.create(f).getSheetAt(0)

  private var totalWheatCropsArea: List[(String, Int)] = List()
  private var totalCropsArea: List[(String, Int)] = List()
  private var totalSurface: List[(String, Int)] = List()


  /** get data from excel file (26 cantons + Switzerland) */
  for (i <- 1 to 27) {
    /** This numbers seems a bit low, check afterwards total use for agricultural purpose */
    totalCropsArea = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(6).toString().toDouble).toInt) :: totalCropsArea
    totalWheatCropsArea = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(
      10).toString().toDouble).toInt) :: totalWheatCropsArea
    totalSurface = (sheet.getRow(i).getCell(0).toString(), math.round(sheet.getRow(i).getCell(14).toString().toDouble).toInt * 100) :: totalSurface
  }

  private val areaOfParcel: Int = 1
  val parcelsToIndex = scala.collection.mutable.Map[CadastralParcel, Int]()
  var sideLength: Int = -1

  /**
   *
   * @param canton
   * @return a tuple of (the new parcels
   */
  def generateParcels(canton: String) = {
    //val parcels = (for (_ <- 1 to totalCropsArea.filter(_._1 == canton).head._2 / areaOfParcel) yield new CadastralParcel(("Douglas", 1111), null, List(), 1)).toList
    val surface: Int = totalSurface.filter(_._1 == canton).head._2
    val parcels = (for (_ <- 1 to surface / areaOfParcel) yield new CadastralParcel(("Douglas", 1111), null, List(), 1)).toList
    parcels.zipWithIndex.foreach(t => parcelsToIndex.put(t._1, t._2))
    //get the neighbors of each parcel and add this info inside parcel
    sideLength = math.sqrt(surface).toInt
    parcels
  }

  /**
   * Once the parcels are generated, add 
   */
  def assignNeighbors: Unit = {

  }

  def getNeighbors(parcel: CadastralParcel): List[CadastralParcel] = {
    List()
  }

  def getNeighborsInRange(parcel: CadastralParcel, radius: Int): List[CadastralParcel] = {
    List()
  }

}