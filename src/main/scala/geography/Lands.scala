import Owner._
//import roadNetwork.Node

package geography {


  case class PointF(x: Double, y: Double) {
    override def toString = s"{$x, $y}"

    def distance(other: PointF): Double = {
      math.sqrt(math.pow(x - other.x, 2) + math.pow(y - other.y, 2))
    }
    }
  
  case class LineF(s: PointF, e: PointF)

  /**
   * @param c0,c1,c2,c3 the 4 coordinates of the shape(assume only rectangular shape atm) (in meter, origin at bottom left of map)
   * distance in meter
   * c1 ---  c2
   * |       |
   * |       |
   * c0 ---  c3
   * From left to right, down to up
   */
  case class Coordinates(c0:PointF, c1:PointF, c2:PointF, c3:PointF){

    val leftBorder : LineF = LineF(c0,c1)
    val rightBorder : LineF = LineF(c3,c2)
    val upBorder : LineF = LineF(c1,c2)
    val downBorder : LineF = LineF(c0,c3)

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

    def getCadastralParcels: List[CadastralParcel] = landsLot.map(_._1).toList

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

  /** Used to perform operation on LandOverlay (split, merge, add/remove
   * parcelles) and get informations (find specific type of landOverlay)
   */
  class LandAdministrator(parcelsData: Any, landOverlaysData: Any) {

    var cadastralParcels: List[CadastralParcel] = List()
    var landOverlays: List[LandOverlay] = List[LandOverlay]()

    /** Declare as an inner object, not stored inside the LandOverlay, cause
     * each time we access a LandOverlay we should pass by the
     * LandAdministrator
     */
    /** Constructor */
    {

      /** Create all parcelles, given data Set Owner to ""
       */
      def createCadastralParcels(data: Any): List[CadastralParcel] = List()

      /** For each paddoc/field/meadow: Find The list of CadastralParcel part of
       * and by how much percentage In function of area of parcelles + perc of
       * CadastralParcel inside overlay + owner find how much does each owner
       * of overlay possess of it (usefull for computing crops income of
       * grouped farmers)
       */
      def createLandOverlays(data: Any): List[LandOverlay] = List()

      cadastralParcels = createCadastralParcels(parcelsData)
      landOverlays = createLandOverlays(landOverlaysData)

    } // end constructor


  /** Split a land overlay in multiple lands overlays of same/different
     * purpose.
     *
     * @param current :
     *                LandOverlay, the land overlay to split
     * @param into    :
     *                List[LandOverlay], the final lands overlays into must only contain
     *                CadastralParcels belonging to current, and each CadastralParcel can
     *                appear in one and only one LandOverlay
     */
    def splitLandOverlay(
                          current: LandOverlay,
                          into: List[(LandOverlay, LandOverlayPurpose.Value)]
                        ): Boolean = {

      assert(landOverlays.contains(current))

      into.foreach(tup => assert(tup._1.getCadastralParcels.length > 0))
      //Check if each cadastral parcels of into is actually in current
      into.foreach(tup =>
        tup._1
          .getCadastralParcels
          .foreach(parcel =>
            assert(current.getCadastralParcels.contains(parcel))
          )
      )

      into.foreach(tup => changePurpose(tup._1, tup._2))

      /** Update the state of partOf inside every parcel involved */
      current.landsLot.foreach(_._1.partOf -= current)
      //into.foreach((land_overlay, purpose) =>
      into.foreach(tup =>
        tup._1.landsLot.foreach(tup2 => tup2._1.partOf += (tup._1 -> tup2._2))
      )

      landOverlays.filterNot(_ == current)

      landOverlays :::= into.map(_._1)

      return true
    }

    /** Remove the land Overlay, and create a new One of different type */
    def changePurpose(
                       landOverlay: LandOverlay,
                       newPurpose: LandOverlayPurpose.Value
                     ): LandOverlay = {
      //remove the landOverlay, but keep its land in memory, as only the purpose changes
      val oldLands: List[(CadastralParcel, Double)] = landOverlay.landsLot
      removeLandOverlay(landOverlay)
      addLandOverlay(oldLands, newPurpose)
    }

    def removeLandOverlay(landOverlay: LandOverlay): Unit = {
      landOverlay.getCadastralParcels.foreach((parcelle: CadastralParcel) => landOverlay.removeCadastralParcel(parcelle))
      landOverlays = landOverlays.filterNot(_ == landOverlay)

    }

    def addLandOverlay(landsDistrib: List[(CadastralParcel, Double)], purpose: LandOverlayPurpose.Value): LandOverlay = {
      val landOverlay: LandOverlay = purpose match {
        case LandOverlayPurpose.wheatField => new Crop(landsDistrib)
        case LandOverlayPurpose.paddock => new Paddock(landsDistrib)
        case LandOverlayPurpose.meadow => new LandOverlay(landsDistrib) //TODO
        case LandOverlayPurpose.noPurpose => new LandOverlay(landsDistrib) //TODO
        //case landAdministrator.LandOverlayPurpose.wheatField => new Crop(landsDistrib)
        //case landAdministrator.LandOverlayPurpose.paddock =>  new Paddock(landsDistrib)
        //case landAdministrator.LandOverlayPurpose.meadow =>  new LandOverlay(landsDistrib) //TODO
        //case landAdministrator.LandOverlayPurpose.noPurpose => new LandOverlay(landsDistrib) //TODO
      }
      landOverlays ::= landOverlay
      landOverlay
    }

    def mergeLandOverlay(
                          toMerge: List[LandOverlay],
                          newPurpose: LandOverlayPurpose.Value
                        ): Boolean = {

      var mergedLandOverlay = new LandOverlay(toMerge.map(_.landsLot).flatten)

      toMerge.foreach(l_over => assert(landOverlays.contains(l_over)))

      changePurpose(mergedLandOverlay, newPurpose)

      toMerge.foreach(lOver => {
        lOver.purpose = newPurpose
      })

      /** remove old land overlay in partOf inside each parcel */
      toMerge.foreach(land_overlay =>
        land_overlay.landsLot.foreach(tup => tup._1.partOf -= land_overlay)
      )

      /** add new land overlay in partOf inside each parcel */
      mergedLandOverlay.landsLot.foreach(tup =>
        tup._1.partOf += (mergedLandOverlay -> tup._2)
      )

      toMerge.foreach(l_over => landOverlays.filterNot(_ == l_over))
      landOverlays ::= mergedLandOverlay

      return true
    }

    /** Changing owner of a Parcelle, will update this Parcelle's owner inside
     * LandOverlay and LandAdministrator Just need to recompute
     * ownershipDistrib inside each land overlays, the Parcelle is part of
     */
    def changeCadastralParcelOwner(
                                    cadastralParcel: CadastralParcel,
                                    new_owner: Owner
                                  ) = {
      cadastralParcel.owner = new_owner
      cadastralParcel.partOf.keys.foreach(key =>
        key.cmptOwnershipDistrib(key.landsLot)
      )
    }

    //TODO
    def findParcelAccess(parcel: CadastralParcel) {}

    /** Only update quantity of grass on each Paddock at the moment, can be more complex afterwards, like changing caracteristics of lands after a rainfall, etc... */
    def update(): Unit = {
      getPaddocks().foreach(_.grassGrowth())
    }

    /**
     * @return The list of Paddocks (inheriting from landOverlay)
     */
    def getPaddocks(): List[Paddock] = {
      landOverlays.filter(_.isInstanceOf[Paddock]).map(_.asInstanceOf[Paddock])
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
}
//https://www.etat.ge.ch/geoportail/pro/?portalresources=CAD_PARCELLE_MENSU

//https://www.geo.vd.ch/?&mapresources=GEOVD_DONNEESCADASTRALES,GEOVD_DONNEESBASE contains parcells and dp

//routes: DP communal, DP cantonal
