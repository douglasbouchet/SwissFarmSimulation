import Owner._
//import roadNetwork.Node

package landAdministrator{
class CadastralParcel(_id: (String, Int), _owner: Owner, adj_parcels: List[CadastralParcel], _area: Double)
{
  /** (commune name, n° inside commune), unique over switzerland */
  val id : (String, Int) = _id

  /** next: List[Owner] instead of a single */
  var owner : Owner = _owner
  val adjacentParcels: List[CadastralParcel] = adj_parcels
  /** (Land overlay -> percentage of cadastral parcel in (0 to 1))
  redundant perc. used for a land overlay, easier to determine if some cadasral parcels have free space */
  var partOf: collection.mutable.Map[LandOverlay, Double] = collection.mutable.Map[LandOverlay, Double]()
  val area: Double = _area
  /** TODO add method in LandAdministrator or whater that should find a ParcelAccess for this parcel
  add it, and add this parcel inside ParcelAcess's "connectedParcels" attribut */
  //val access: Node = new Node("sds")
}

/** group the cadastral parcels which are physically the same field/paddoc/meadow, and belong to one or multiple owners */
class LandOverlay(aggregation: List[(CadastralParcel, Double)]) {

  /** (CadastralParcel, Percentage occupied on it (0 to 1)) */ 
  var landsLot : List[(CadastralParcel, Double)] = aggregation
  /** How much percentage of the aggregated lands each owner has */
  var ownershipDistrib : List[(Owner, Double)] = cmptOwnershipDistrib(landsLot)

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
}

/** Used to perform operation on LandOverlay (split, merge, add/remove parcelles)
and get informations (find specific type of landOverlay) */
class LandAdministrator(parcelle_data: Object, land_overlay_data: Object) {
  
  var cadastralParcels: List[CadastralParcel] = List()
  var landOverlays : List[LandOverlay] = List()
  var purposeOfLandOverlay: collection.mutable.Map[LandOverlay, LandOverlayPurpose] = collection.mutable.Map[LandOverlay, LandOverlayPurpose]()

  /** Declare as an inner object, not stored inside the LandOverlay, cause each time we access a LandOverlay
  we should pass by the LandAdministrator */ 
  class LandOverlayPurpose(data: Any) extends Enumeration {
    type LandOverlayPurpose = Value
    val WheatField, Paddoc, Meadow = Value /** + some other types of cereals to add */
  }

  /** Constructor */
  {
    /** Create all parcelles, given data
    Set Owner to ""
    */
    def createCadastralParcels(data: Object): List[CadastralParcel] = ???

    /** 
    For each paddoc/field/meadow:
      Find The list of CadastralParcel part of and by how much percentage  
      In function of area of parcelles + perc of CadastralParcel inside overlay + owner find 
      how much does each owner of overlay possess of it (usefull for computing crops income of grouped farmers)
    */
    def createLandOverlays(data: Object): List[LandOverlay] = ???

  
    var cadastralParcels: List[CadastralParcel] = createCadastralParcels(parcelle_data)
    var landOverlays : List[LandOverlay] = ???

    
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
  //new ownership, new parcelle,...
  //def changeOrganisation()

  //TODO
  def findParcelAccess(parcel: CadastralParcel){}

}


//private roads? as used in algo, length might be negligeable (or for big private parcelles,
//start from middle of parcelle, and compute length to nearest DP road, but how to ? )
class Road {

}
//class Road extends CadastralParcel {
//
///** Second element a string now (e.g DP 234) */
//override val id : (String, String)
//
//val adjacent_road_dist: List[(Road, Double)] // List of (Road, distance(m) from center of this to Road) cf plan ipad 
//
////TODO Find how to compute length between middle of the road and other roads
//
////val type : {national, cantonal, communal} usefull ? 
//val max_weight_allowed : Int // in kg 

//... futher: capacity, risk of snow, status (bloqued for repair,...)
//}

//class Railway extends CadastralParcel {
//
//  //a railway is defined between 2 Network node cf https://map.geo.admin.ch/?lang=fr&selectedNode=&topic=inspire&layers=ch.kantone.cadastralwebmap-farbe,ch.swisstopo-vd.geometa-grundbuch,ch.are.bauzonen,ch.bafu.hydroweb-messstationen_grundwasserzustand,ch.bfs.volkszaehlung-gebaeudestatistik_gebaeude,ch.swisstopo.vec25-gebaeude,ch.bfs.gebaeude_wohnungs_register,ch.blw.bodeneignung-gruendigkeit,ch.blw.bodeneignung-kulturland,ch.blw.bewaesserungsbeduerftigkeit,ch.swisstopo.geologie-rohstoffe-salz_abbau_verarbeitung,ch.swisstopo.geologie-geotechnik-gk500-genese,ch.swisstopo.geologie-rohstoffe-industrieminerale,ch.swisstopo.geologie-geosites,ch.swisstopo.geologie-gesteinsdichte,ch.swisstopo.geologie-geophysik-geothermie,ch.blw.bodeneignung-wasserspeichervermoegen,ch.blw.bodeneignung-vernaessung,ch.blw.bodeneignung-kulturtyp,ch.blw.ursprungsbezeichnungen-pflanzen,ch.swisstopo.swisstlm3d-eisenbahnnetz,ch.bav.schienennetz,ch.astra.hauptstrassennetz,ch.astra.strassenverkehrszaehlung-uebergeordnet&bgLayer=ch.swisstopo.swissimage&E=2522854.13&N=1156967.82&zoom=11.039025467272955&catalogNodes=1,58,108,287,306,109&layers_opacity=1,0.75,0.6,1,1,1,1,0.75,0.75,0.75,1,0.75,1,1,0.75,1,0.75,0.75,0.75,0.75,1,1,0.75,1&layers_visibility=true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false&layers_timestamp=,,,,2019,,,,,,,,,,,,,,,,,,,
//  val startNode: Int 
//  val endNode: Int
//  val startKm: Double
//  val endKm: Double
//  val length : Double = endKm - startKm 
//  // ... number of ways etc.. 
//}

class Land(_id: (String, Int), _owner: Owner, _adj_parcels: List[CadastralParcel], _area: Double)
 extends CadastralParcel(_id,_owner,_adj_parcels,_area) {

  //val zoneType: ZoneType //TODO Does Road also have this type ? 

  //val features : Information
  // val pieces_of_land : List[Patch] cut land into small pieces, with different features
  // val pieces_of_land : List[List[Patch]] same but in 3d 

  // polygon: List[Point]

  //val buildings : List[Building] // Building type TBD

  /** BFS search over neighbors parcelles
  * @return first parcelle of type Road found 
  */
  // def findNearestRoad(): Road = ???
}

class Information {

  // polygon: List[Point]
  
  // This attributs contains information about the land, extend them with what we need/is available
  //val geological_info: Geological = Geological
  //val ground_info: Ground = Ground
  //val crops : Crops = Crops

class Geological {
  // examples
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
  //...
}

class Crops {
  //val type : CropsType
  //Impact on land and productivity
  //var fertilizer : List[Fertilizer] = List()
  //svar pesticide : List[Pesticide] = List()
}
}


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

//class Building{
//
//  //val type: page 13 vd.ch/fileadmin/user_upload/organisation/dinf/sit/fichiers_pdf/Merkmalkatalog_V_3-6_F.pdf  
//  val surface : Double 
//  val coordinates: (Int, Int)  //p.24 vd.ch/fileadmin/user_upload/organisation/dinf/sit/fichiers_pdf/Merkmalkatalog_V_3-6_F.pdf 
//  val status // p.27
//  //... (year of construction, ....)
//}




//https://www.etat.ge.ch/geoportail/pro/?portalresources=CAD_PARCELLE_MENSU

//https://www.geo.vd.ch/?&mapresources=GEOVD_DONNEESCADASTRALES,GEOVD_DONNEESBASE contains parcells and dp 

//routes: DP communal, DP cantonal






//PATH FINDER 
//
//algo pour trouver path: 
//On part d'une adresse, si DP (cantonal ou departemental) dans la list des voisins, alors on a la première route, et on la suit jusqu'a destination
//Sinon on fait un algo type BFS/DFS, qui check si un node est voisin ou est une DP 
//Une fois sur la road, enchainement de DP ou parcelle privée si fin de DP, until arrive to the destination
//
//Une fois le trajet complété, cache le path dans l'agent qui effectu le trajet.
//
//Compute the length of roads:
//
//How to get the shortest path, or at least decent ones ? Need a graph representation ? 
//-> consider intersection as node, and roads as edges
//Use the idea that when you travel the classic path is: 
//- smaller road into bigger ones
//- travell on bigger one 
//- bigger one to small one to arrive at destination 
//A* algorithm 
//
//
//Fonctionnement global: 
//Un graph qui represent l'ensemble des roads et intersection. Les edges doivent influencé le choix
// i.e maybe go as fast as possible on a highway, and exit near to the destination. 
// Par rapport aux parcelles, les roads présentes sur des parcelles privés ne peuvent en théorie pas être emprunté 
// par celui qui se déplace. A PART au début -> c'est pour ca qu'au début on fait un BFS/DFS sur les voisins pour trouver la
// route la plus proche.
//
// Representé route par des "parcelles" utile pour calculer la distance entre 2 intersections, et du coup 
// remplir le graph 
//
// Algo :
//1. on trouve le chemin non privé le plus proche (de proche en proche à travers les parcells) pour le départ
// et l'arrivé. 
//2. On se sert du graph pour joindre les 2 verticies(correspondant aux vertices auquels 
//les routes de départ aboutissent)
//
//
//Pour les parcelles "paumé", utiliser l'étape 1 plus longtemps 
//
//
//We can also transport with train: 
//
//- find optimal path in train. FInd the start point and end point (where stuff is loaded on train)
//- apply algo above to find roads from farm or whatever to start point, and from end point to final destination



// TODO !!!! No infos exists on size of private roads....
}