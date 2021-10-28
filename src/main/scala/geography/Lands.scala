import Owner._
//import roadNetwork.Node
import generation.LandGenerator

package geography {

  import Simulation.SimLib.Mill
  import Simulation.SimO
  import FarmRelated.Farmer

  import scala.collection.mutable


  case class PointF(x: Double, y: Double) {
    override def toString = s"{$x, $y}"

    def distance(other: PointF): Double = {
      math.sqrt(math.pow(x - other.x, 2) + math.pow(y - other.y, 2))
    }
  }

  case class LineF(s: PointF, e: PointF)

  /**
   * @param c0  ,c1,c2,c3 the 4 coordinates of the shape(assume only rectangular shape atm) (in meter, origin at bottom left of map)
   *           distance in meter
   *           c1 ---  c2
   *           |       |
   *           |       |
   *           c0 ---  c3
   *           From left to right, down to up
   */
  case class Coordinates(c0: PointF, c1: PointF, c2: PointF, c3: PointF) {

    val leftBorder: LineF = LineF(c0, c1)
    val rightBorder: LineF = LineF(c3, c2)
    val upBorder: LineF = LineF(c1, c2)
    val downBorder: LineF = LineF(c0, c3)

    def findIntersection(l1: LineF, l2: LineF): PointF = {
      val a1 = l1.e.y - l1.s.y
      val b1 = l1.s.x - l1.e.x
      val c1 = a1 * l1.s.x + b1 * l1.s.y
      val a2 = l2.e.y - l2.s.y
      val b2 = l2.s.x - l2.e.x
      val c2 = a2 * l2.s.x + b2 * l2.s.y

      val delta = a1 * b2 - a2 * b1
      // If lines are parallel, intersection point will contain infinite values
      PointF((b2 * c1 - b1 * c2) / delta, (a1 * c2 - a2 * c1) / delta)
    }

    def computeArea(): Double = c0.distance(c1) * c0.distance(c3) //Here we assume rectangular shape, in m^2
  }


  class CadastralParcel(
                         _id: (String, Int),
                         _owner: Owner,
                         adj_parcels: List[CadastralParcel],
                         _area: Double,
                         // _coordinates: Coordinates
                       ) {

    /** (commune name, n° inside commune), unique over switzerland */
    val id: (String, Int) = _id
    val adjacentParcels: List[CadastralParcel] = adj_parcels
    val area: Double = _area
    //val coordinates : Coordinates = _coordinates
    /** TODO add method in LandAdministrator or whater that should find a
     * ParcelAccess for this parcel add it, and add this parcel inside
     * ParcelAcess's "connectedParcels" attribut
     */
    //val access: ParcelAccess = new ParcelAccess("", list())
    val landInformation: List[Patch] = List()
    /** next: List[Owner] instead of a single */
    var owner: Owner = _owner
    /** (Land overlay -> percentage of cadastral parcel in (0 to 1)) */
    var partOf: collection.mutable.Map[LandOverlay, Double] =
      collection.mutable.Map[LandOverlay, Double]()

    class Patch(_area: Double) {
      val area: Double = _area
      val geologicalInfo: Geological = new Geological()
      val groundInfo: Ground = new Ground()

      class Geological {
        //var groundwater_levels : Double; // m
        //var heat_flux_density : Double; // mW/m^2
        //var rock_density: Double; // kg/m^3
        //var temperature_at_100m : Double;
        //...
      }

      class Ground {
        //var steepness : Int;
        //var altitude : Int;
        //var annual_rainfall : Double;// mm/year
        //var water_retention_capacity : Double;// 1 == 1 mm of water accesible for plants at 1 cm below the ground
        //var permeability : Double;// coefficient
        //var depth_soil_exploitable_roots : Double;
      }
    }
  }

  /* def splitCadastralParcel(): List[CadastralParcel] =  {
      //select a random point inside the parcel, and split by drawing verticals and horizontals lines
      val rdm = scala.util.Random
      val splitPoint = PointF(rdm.between(_coordinates.c0.x, _coordinates.c3.x), rdm.between(_coordinates.c0.y, _coordinates.c1.y))
      val verticalLine : LineF = LineF(splitPoint, PointF(splitPoint.x, splitPoint.y + 1))
      val horizontalLine : LineF = LineF(splitPoint, PointF(splitPoint.x + 1, splitPoint.y))

      val upIntersection: PointF = _coordinates.findIntersection(verticalLine, _coordinates.upBorder)
      val downIntersection: PointF = _coordinates.findIntersection(verticalLine, _coordinates.downBorder)
      val leftIntersection: PointF = _coordinates.findIntersection(horizontalLine, _coordinates.leftBorder)
      val rightIntersection: PointF = _coordinates.findIntersection(horizontalLine, _coordinates.rightBorder)

      val botLeftCoord = Coordinates(_coordinates.c0, leftIntersection, splitPoint, downIntersection)
      val upLeftCoord = Coordinates(leftIntersection, _coordinates.c1, upIntersection, splitPoint)
      val upRightCoord = Coordinates(splitPoint, upIntersection, _coordinates.c2, rightIntersection)
      val downRightCoord = Coordinates(downIntersection, splitPoint, rightIntersection, _coordinates.c3)

      List(
        new CadastralParcel(id, owner, adj_parcels, botLeftCoord.computeArea(), botLeftCoord),
        new CadastralParcel(id, owner, adj_parcels, upLeftCoord.computeArea(), upLeftCoord),
        new CadastralParcel(id, owner, adj_parcels, upRightCoord.computeArea(), upRightCoord),
        new CadastralParcel(id, owner, adj_parcels, downRightCoord.computeArea(), downRightCoord),
      )
    }
  }
  */


  /** group the cadastral parcels which are physically the same
   * field/paddoc/meadow, and belong to one or multiple owners
   */
  class LandOverlay(aggregation: List[(CadastralParcel, Double)]) {

    /** (CadastralParcel, Percentage occupied on it (0 to 1)) */
    var landsLot: List[(CadastralParcel, Double)] = aggregation

    landsLot.foreach {
      case (parcel: CadastralParcel, proportion: Double) => parcel.partOf.put(this, proportion)
    }

    /** How much percentage of the aggregated lands each owner has */
    var ownershipDistrib: List[(Owner, Double)] = cmptOwnershipDistrib(landsLot)

    var purpose: LandOverlayPurpose.Value = LandOverlayPurpose.noPurpose
    var prevPurpose: LandOverlayPurpose.Value = LandOverlayPurpose.noPurpose

    var soilQuality: Double = 1.0

    def addCadastralParcel(
                            cadastralParcel: CadastralParcel,
                            proportion: Double
                          ) = {
      assert(!landsLot.contains((cadastralParcel, proportion)))
      landsLot ::= (cadastralParcel, proportion)
      ownershipDistrib = cmptOwnershipDistrib(landsLot)
    }

    /** Store the characteristics of fields inside the landOverlay class
     * depending on the purpose of the LandOverlay
     */

    def cmptOwnershipDistrib(
                              newLandsLot: List[(CadastralParcel, Double)]
                            ): List[(Owner, Double)] = {

      val totalArea: Double = getSurface

      val ownerAreaUse: List[(Owner, Double)] =
        newLandsLot.map(tup => (tup._1.owner, tup._1.area * tup._2))
      var areaPerOwner = collection.mutable.Map[Owner, Double]()
      ownerAreaUse.foreach(tup => {
        areaPerOwner += (tup._1 -> (areaPerOwner
          .getOrElse(tup._1, 0.0) + tup._2))
      })
      areaPerOwner.toList.map(tup => (tup._1, tup._2 / totalArea))
    }

    def getSurface: Double = landsLot.foldLeft(0.0) { (acc, tup) =>
      acc + tup._1.area * tup._2
    }

    def removeCadastralParcel(cadastralParcel: CadastralParcel): Unit = {
      cadastralParcel.partOf.get(this) match {
        case Some(percentage) => {
          assert(landsLot.contains((cadastralParcel, percentage)))
          cadastralParcel.partOf.remove(this)
          landsLot = landsLot.filterNot(_ == (cadastralParcel, percentage))
          ownershipDistrib = cmptOwnershipDistrib(landsLot)
        }
        case None =>
          println(
            "trying to remove a cadastral parcel that is not part of this land overlay"
          )
      }
    }

    def getCadastralParcels: List[CadastralParcel] = landsLot.map(_._1)

    class Meadow {
      //var remainingQuantityOfGrass: Double
      //...
    }
  }

  class Crop(aggregation: List[(CadastralParcel, Double)]) extends LandOverlay(aggregation) {
    // var cropsType = TODO say if it grows wheat, barley, ...
    purpose = LandOverlayPurpose.wheatField
    //Impact on land and productivity TODO implement in function of what we want to influence (e.g quantity of nutrients, .... )
    // var nutrients ???
  }

  class Paddock(aggregation: List[(CadastralParcel, Double)]) extends LandOverlay(aggregation) {
    val maxGrassQuantity: Double = CONSTANTS.KG_GRASS_PER_PADDOCK_HA * super.getSurface
    var grassQuantity: Double = CONSTANTS.KG_GRASS_PER_PADDOCK_HA * super.getSurface
    purpose = LandOverlayPurpose.paddock

    /** growth some grass each turn, in order to each max capacity in 30 days from grass quantity 0 */
    def grassGrowth(): Unit = {
      grassQuantity = math.min(maxGrassQuantity, grassQuantity + maxGrassQuantity / CONSTANTS.TIME_FOR_PADDOCK_TO_RECOVER_GRASS)
    }
  }

  object LandOverlayPurpose extends Enumeration {
    type LandOverlayPurpose = Value
    val wheatField = Value("Wheat field")
    val paddock = Value("Paddock")
    val meadow = Value("Meadow")
    val noPurpose = Value("no purpose")
  }
}

/** Class Road not needed anymore Dont needed,we could just iterate over all
 * parcels and check id ("DP 2222") to see if a road, and implement it in the
 * BFS algo to find access
 */

// Use to define what can be made on a land, e.g cannot construct houses on agricultural zone
// All zones: https://ge.ch/sitg/geodata/SITG/CATALOGUE/INFORMATIONS_COMPLEMENTAIRES/DESCRIPTIF_ZONES_AFFECTATION.pdf
object ZoneType extends Enumeration {
  type ZoneType = Value

  // not complete
  val AgriculturalZone, IndustrialCraftZone, FourAZone = Value
}

object CropsType extends Enumeration {
  type CropsType = Value

  // not complete
  val Wheat, Barley, Potato = Value
}


//https://www.etat.ge.ch/geoportail/pro/?portalresources=CAD_PARCELLE_MENSU

//https://www.geo.vd.ch/?&mapresources=GEOVD_DONNEESCADASTRALES,GEOVD_DONNEESBASE contains parcells and dp

//routes: DP communal, DP cantonal
