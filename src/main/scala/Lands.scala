import Owner._
//import roadNetwork.Node

package landAdministrator{

class CadastralParcel(_id: (String, Int), _owner: Owner, adj_parcels: List[CadastralParcel], _area: Double) {
  /** (commune name, n° inside commune), unique over switzerland */
  val id : (String, Int) = _id

  /** next: List[Owner] instead of a single */
  var owner : Owner = _owner
  val adjacentParcels: List[CadastralParcel] = adj_parcels
  /** (Land overlay -> percentage of cadastral parcel in (0 to 1)) */
  var partOf: collection.mutable.Map[LandOverlay, Double] = collection.mutable.Map[LandOverlay, Double]()
  val area: Double = _area
  /** TODO add method in LandAdministrator or whater that should find a ParcelAccess for this parcel
  add it, and add this parcel inside ParcelAcess's "connectedParcels" attribut */
  //val access: ParcelAccess = new ParcelAccess("", list())
  val landInformation: List[Patch] = List()

  class Patch(_area: Double){
    val area: Double = _area
    val geologicalInfo: Geological = new Geological()
    val groundInfo: Ground = new Ground()

    class Geological{
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

/** group the cadastral parcels which are physically the same field/paddoc/meadow, and belong to one or multiple owners */
class LandOverlay(aggregation: List[(CadastralParcel, Double)]) {

  /** (CadastralParcel, Percentage occupied on it (0 to 1)) */ 
  var landsLot : List[(CadastralParcel, Double)] = aggregation
  /** How much percentage of the aggregated lands each owner has */
  var ownershipDistrib : List[(Owner, Double)] = cmptOwnershipDistrib(landsLot)

  /** Store the characteristics of fields inside the landOverlay class 
  depending on the purpose of the LandOverlay */


  def cmptOwnershipDistrib(newLandsLot: List[(CadastralParcel, Double)]): List[(Owner, Double)] = {

    val totalArea: Double = getSurface(newLandsLot)

    val ownerAreaUse: List[(Owner, Double)] = newLandsLot.map(tup => (tup._1.owner, tup._1.area * tup._2))
    var areaPerOwner = collection.mutable.Map[Owner, Double]()
    ownerAreaUse.foreach(
      tup => {areaPerOwner += (tup._1 -> (areaPerOwner.getOrElse(tup._1,0.0) + tup._2))}
    )
    areaPerOwner.toList.map(tup => (tup._1, tup._2 / totalArea))
  }

  def addCadastralParcel(cadastralParcel: CadastralParcel, proportion: Double) = {
      assert(! landsLot.contains((cadastralParcel,proportion)))
      landsLot ::= (cadastralParcel, proportion)
      ownershipDistrib = cmptOwnershipDistrib(landsLot)
  }
  
  def removeCadastralParcel(cadastralParcel: CadastralParcel) = {
    cadastralParcel.partOf.get(this) match {
      case Some(percentage) => {
        assert(landsLot.contains((cadastralParcel, percentage)))
        landsLot.filterNot(_ == (cadastralParcel, percentage))
        ownershipDistrib = cmptOwnershipDistrib(landsLot)
      }
      case None => println("trying to remove a cadastral parcel that is not part of this land overlay")
    }
  }

  def getCadastralParcels() : List[CadastralParcel] = landsLot.map(_._1).toList

  def getSurface(lands: List[(CadastralParcel, Double)]): Double = lands.foldLeft(0.0){(acc, tup) => acc + tup._1.area * tup._2}

  class Crops {
  //val type : CropsType
  //Impact on land and productivity
  //var fertilizer : List[Fertilizer] = List()
  //svar pesticide : List[Pesticide] = List()
  }

  class Paddoc {
  //var typeAnimals: List[Animal]
  //var nAnimals: Int
  //...
  }

  class Meadow {
  //var remainingQuantityOfGrass: Double
  //...
  }
}

/** Used to perform operation on LandOverlay (split, merge, add/remove parcelles)
and get informations (find specific type of landOverlay) */
class LandAdministrator(parcelsData: Any, landOverlaysData: Any) {
  
  var cadastralParcels: List[CadastralParcel] = List()
  var landOverlays : List[LandOverlay] = List()
  var purposeOfLandOverlay: collection.mutable.Map[LandOverlay, LandOverlayPurpose] = collection.mutable.Map[LandOverlay, LandOverlayPurpose]()

  /** Declare as an inner object, not stored inside the LandOverlay, cause each time we access a LandOverlay
  we should pass by the LandAdministrator */ 
  class LandOverlayPurpose() extends Enumeration {
    type LandOverlayPurpose = Value
    val WheatField, Paddoc, Meadow = Value /** + some other types of cereals to add */
  }

  /** Constructor */
  {
    /** Create all parcelles, given data
    Set Owner to ""
    */
    def createCadastralParcels(data: Any): List[CadastralParcel] = ???

    /** 
    For each paddoc/field/meadow:
      Find The list of CadastralParcel part of and by how much percentage  
      In function of area of parcelles + perc of CadastralParcel inside overlay + owner find 
      how much does each owner of overlay possess of it (usefull for computing crops income of grouped farmers)
    */
    def createLandOverlays(data: Any): List[LandOverlay] = ???

  
    cadastralParcels = createCadastralParcels(parcelsData)
    landOverlays = createLandOverlays(landOverlaysData)
    
  }// end constructor

  /** Split a land overlay in multiple lands overlays of same/different purpose. 
  * @param current: LandOverlay, the land overlay to split 
  * @param into: List[LandOverlay], the final lands overlays 
  * into must only contain CadastralParcels belonging to current, and each CadastralParcel can appear in one and only one 
  * LandOverlay
  */
  def splitLandOverlay(current: LandOverlay, into: List[(LandOverlay, LandOverlayPurpose)]): Boolean = {
    
    assert(landOverlays.contains(current))

    into.foreach(tup => assert(tup._1.getCadastralParcels().length > 0))
    //Check if each cadastral parcels of into is actually in current
    into.foreach(tup => 
      tup._1.getCadastralParcels().foreach(parcel =>
         assert(current.getCadastralParcels().contains(parcel))))

    into.foreach(tup => changePurpose(tup._1, tup._2))

    /** Update the state of partOf inside every parcel involved */
    current.landsLot.foreach(_._1.partOf -= current)
    //into.foreach((land_overlay, purpose) => 
    into.foreach(tup => 
      tup._1.landsLot.foreach(tup2 => 
        tup2._1.partOf += (tup._1 -> tup2._2)))
  
    landOverlays.filterNot(_ == current)

    landOverlays :::= into.map(_._1)

    return true
  }

  def mergeLandOverlay(toMerge: List[LandOverlay], newPurpose: LandOverlayPurpose): Boolean = {

    var mergedLandOverlay = new LandOverlay(toMerge.map(_.landsLot).flatten)

    toMerge.foreach(l_over => assert(landOverlays.contains(l_over)))
      
    changePurpose(mergedLandOverlay, newPurpose)

    /** remove old land overlay in partOf inside each parcel */
    toMerge.foreach(land_overlay => 
      land_overlay.landsLot.foreach(tup => tup._1.partOf -= land_overlay))
    /** add new land overlay in partOf inside each parcel */
    mergedLandOverlay.landsLot.foreach(tup =>
      tup._1.partOf += (mergedLandOverlay -> tup._2))

    toMerge.foreach(l_over => landOverlays.filterNot(_ == l_over))
    landOverlays ::= mergedLandOverlay

    return true
  }

  def changePurpose(landOverlay: LandOverlay, newPurpose: LandOverlayPurpose) = {
    purposeOfLandOverlay.get(landOverlay) match {
      case Some(purpose) => {
        purposeOfLandOverlay += (landOverlay -> newPurpose)
        println(landOverlay + " changes its purpose from " + purpose + " to " + newPurpose)
      }
      case None => println("Error, this landOverlay does not exist")
    }
  }

  /** def get_land_overlay_of_purpose(purpose: LandOverlayPurpose, region: Canton or district,...)
    Could be interesting if want to have some stats per canton/....
  */
  def getLandOverlayOfPurpose(purpose: LandOverlayPurpose): List[LandOverlay] = {
    return purposeOfLandOverlay.filter(_._2 == purpose).toList.map(_._1)
  }
    
  /** 
  * Changing owner of a Parcelle, will updtate this Parcelle's owner inside LandOverlay and LandAdministrator
  * Just need to recompute ownershipDistrib inside each land overlays, the Parcelle is part of
  */
  def changeCadastralParcelOwner(cadastralParcel: CadastralParcel, new_owner: Owner) = {
    cadastralParcel.owner = new_owner
    cadastralParcel.partOf.keys.foreach(key => key.cmptOwnershipDistrib(key.landsLot))
  }

  //TODO 
  def findParcelAccess(parcel: CadastralParcel){}


}

/** Class Road not needed anymore 
 Dont needed,we could just iterate over all parcels and check id ("DP 2222") to see if a road, and implement it in
 the BFS algo to find access */


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