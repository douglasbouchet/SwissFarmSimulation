class CadastralParcel
{
  val id : (String, Int) // (commune name, n° inside commune), unique over switzerland

  var owner : Owner // obtained in land register, TODO see if add multiple owners
  val adjacent_cadastral_parcels: List[CadastralParcel]
  // (Land overlay -> percentage of cadastral parcel in (0 to 1))
  /** redundant perc. used for a land overlay, easier to determine if some cadasral parcels have free space*/
  var part_of: collection.mutable.Map[LandOverlay, Double] = collection.mutable.Map[LandOverlay, Double]()
  val area: Double
}

/** group the cadastral parcels which are physically the same field/paddoc/meadow, and belong to one or multiple owners */
class LandOverlay(aggregation: List[(CadastralParcel, Double)]) {

  /** (CadastralParcel, Percentage occupied on it (0 to 1)) */ 
  var cadastral_parcels_aggregation : List[(CadastralParcel, Double)] = aggregation
  /** How much percentage of the aggregated lands each owner has */
  var ownership_distribution : List[(Owner, Double)] = cmpt_ownership_distrib(cadastral_parcels_aggregation)

  //If not compiling, put it above
  def cmpt_ownership_distrib(new_cadastral_parcels_aggregation: List[(CadastralParcel, Double)]): List[(Owner, Double)] = {

    val total_area: Double = new_cadastral_parcels_aggregation.foldLeft(0.0){(acc, tup) => acc + tup._1.area * tup._2}

    val owner_area_use: List[Owner, Double)] =
      new_cadastral_parcels_aggregation.map((parcel, parcel_used) => (parcel.owner, parcel.area * parcel_used))
    var area_per_owner = collection.mutable.Map[Owner, Double]()
    owner_area_use.foreach{
      tup => map += (tup._1 -> area_per_owner.getOrElse(tup._1,0.0) + tup._2)
    }
    use_per_owner.toList.map(tup => (tup._1, tup._2 / total_area))
  }

  def addCadastralParcel(cadastral_parcel: CadastralParcel, proportion: Double) = {
      assert(! cadastral_parcels_aggregation.contains((cadastral_parcel,proportion)))
      cadastral_parcels_aggregation ::= (cadastral_parcel, proportion)
      ownership_distribution = cmpt_ownership_distrib(cadastral_parcels_aggregation)
  }
  
  def removeCadastralParcel(parcelle: CadastralParcel) = {
    cadastral_parcel.get(this) match {
      case Some(x) => {
        assert(cadastral_parcels_aggregation.contains((cadastral_parcel, x)))
        cadastral_parcels_aggregation.filterNot(_ == (cadastral_parcel, proportion))
        ownership_distribution = cmpt_ownership_distrib(cadastral_parcels_aggregation)
      }
      case None => println("trying to remove a cadastral parcel that is not part of this land overlay")
    }
  }

  def getCadastralParcels() = cadastral_parcels_aggregation.map(_._1).toList

/** Used to perform operation on LandOverlay (split, merge, add/remove parcelles)
and get informations (find specific type of landOverlay) */
object LandAdministrator(parcelle_data: Object, land_overlay_data: Object) {
  
  /** Declare as an inner object, not stored inside the LandOverlay, cause each time we access a LandOverlay
  we should pass by the LandAdministrator */ 
  object LandOverlayPurpose extends Enumeration {
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

  
    var all_cadastral_parcels: List[CadastralParcel] = createParcelles()
    var all_land_overlays : List[LandOverlay] = ???

    var purpose_of_land_overlay: collection.mutable.Map[LandOverlay, LandOverlayPurpose] =
      collection.mutable.Map[LandOverlay, LandOverlayPurpose]()
    
  }// end constructor

  /** Split a land overlay in multiple lands overlays of same/different purpose. 
  * @param current: LandOverlay, the land overlay to split 
  * @param into: List[LandOverlay], the final lands overlays 
  * into must only contain CadastralParcels belonging to current, and each CadastralParcel can appear in one and only one 
  * LandOverlay
  */
  def splitLandOverlay(current: LandOverlay, into: List[(LandOverlay, LandOverlayPurpose)]): Boolean = {
    
    assert(all_land_overlays.contains(current))

    into.foreach(assert(_._1.getCadastralParcels().length > 0)))
    //Check if each cadastral parcels of into is actually in current
    assert(into.foreach(_._1.getCadastralParcels().foreach(current.getCadastralParcels().contains(_))))
    
    into.foreach((land_overlay, purp) => changePurpose(land_overlay, purp))

    /** Update the state of part_of inside every parcel involved */
    current.cadastral_parcels_aggregation.foreach(_.part_of -= current)
    into.foreach((land_overlay, purpose) => 
      land_overlay.cadastral_parcels_aggregation.foreach((parcel, perc) => 
        parcel.part_of += (land_overlay -> perc)))
  
    all_land_overlays.remove(current)

    into.foreach(all_land_overlays ::= _)

    return true
  }

  def mergeLandOverlay(to_merge: List[LandOverlay], new_purpose: LandOverlayPurpose): Boolean = {

    var new_aggregation: List[(CadastralParcel, Double)] = to_merge.flatten
    var merged_land_overlay = new LandOverlay(new_aggregation, List())

    assert(to_merge.foreach(all_land_overlays.contains(_)))
      
    changePurpose(merged_land_overlay, new_purpose)

    /** remove old land overlay in part_of inside each parcel */
    to_merge.foreach(land_overlay => 
      land_overlay.cadastral_parcels_aggregation.foreach(_.part_of -= land_overlay))
    /** add new land overlay in part_of inside each parcel */
    merged_land_overlay.foreach(land_overlay => 
      land_overlay.cadastral_parcels_aggregation.foreach((parcel, perc) => 
        parcel.part_of += (land_overlay -> perc)))

    to_merge.foreach(all_land_overlays.remove(_))
    all_land_overlays ::= merged_land_overlay

    return true
  }

  def changePurpose(landOverlay: LandOverlay, new_purpose: LandOverlayPurpose) = {
    purpose_of_land_overlay.get(landOverlay) match {
      case Some(purpose) => {
        purpose_of_land_overlay += (landOverlay -> new_purpose)
        println(landOverlay + " changes its purpose from " + purpose + " to " + new_purpose)
      }
      case None => println("Error, this landOverlay does not exist")
    }
  }

  /** def get_land_overlay_of_purpose(purpose: LandOverlayPurpose, region: Canton or district,...)
    Could be interesting if want to have some stats per canton/....
  */
  def getLandOverlayOfPurpose(purpose: LandOverlayPurpose): List[LandOverlay] = {
    return purpose_of_land_overlay.filter(_._2 == purpose).map(_.1)
  }
    
  /** 
  * Changing owner of a Parcelle, will updtate this Parcelle's owner inside LandOverlay and LandAdministrator
  * Just need to recompute ownership_distribution inside each land overlays, the Parcelle is part of
  */
  def changeCadastralParcelOwner(cadastral_parcel: CadastralParcel, new_owner: Owner) = {
    cadastral_parcel.owner = new_owner
    cadastral_parcel.part_of.keys.foreach(_.cmpt_ownership_distrib(_.cadastral_parcels_aggregation))
  }
  //new ownership, new parcelle,...
  def changeOrganisation()

}


//private roads? as used in algo, length might be negligeable (or for big private parcelles,
//start from middle of parcelle, and compute length to nearest DP road, but how to ? )
class Road extends CadastralParcel {

/** Second element a string now (e.g DP 234) */
override val id : (String, String)

val adjacent_road_dist: List[(Road, Double)] // List of (Road, distance(m) from center of this to Road) cf plan ipad 

//TODO Find how to compute length between middle of the road and other roads

//val type : {national, cantonal, communal} usefull ? 
val max_weight_allowed : Int // in kg 

//... futher: capacity, risk of snow, status (bloqued for repair,...)
}

class Railway extends CadastralParcel {

  //a railway is defined between 2 Network node cf https://map.geo.admin.ch/?lang=fr&selectedNode=&topic=inspire&layers=ch.kantone.cadastralwebmap-farbe,ch.swisstopo-vd.geometa-grundbuch,ch.are.bauzonen,ch.bafu.hydroweb-messstationen_grundwasserzustand,ch.bfs.volkszaehlung-gebaeudestatistik_gebaeude,ch.swisstopo.vec25-gebaeude,ch.bfs.gebaeude_wohnungs_register,ch.blw.bodeneignung-gruendigkeit,ch.blw.bodeneignung-kulturland,ch.blw.bewaesserungsbeduerftigkeit,ch.swisstopo.geologie-rohstoffe-salz_abbau_verarbeitung,ch.swisstopo.geologie-geotechnik-gk500-genese,ch.swisstopo.geologie-rohstoffe-industrieminerale,ch.swisstopo.geologie-geosites,ch.swisstopo.geologie-gesteinsdichte,ch.swisstopo.geologie-geophysik-geothermie,ch.blw.bodeneignung-wasserspeichervermoegen,ch.blw.bodeneignung-vernaessung,ch.blw.bodeneignung-kulturtyp,ch.blw.ursprungsbezeichnungen-pflanzen,ch.swisstopo.swisstlm3d-eisenbahnnetz,ch.bav.schienennetz,ch.astra.hauptstrassennetz,ch.astra.strassenverkehrszaehlung-uebergeordnet&bgLayer=ch.swisstopo.swissimage&E=2522854.13&N=1156967.82&zoom=11.039025467272955&catalogNodes=1,58,108,287,306,109&layers_opacity=1,0.75,0.6,1,1,1,1,0.75,0.75,0.75,1,0.75,1,1,0.75,1,0.75,0.75,0.75,0.75,1,1,0.75,1&layers_visibility=true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false&layers_timestamp=,,,,2019,,,,,,,,,,,,,,,,,,,
  val startNode: Int 
  val endNode: Int
  val startKm: Double
  val endKm: Double
  val length : Double = endKm - startKm 
  // ... number of ways etc.. 
}

class Land extends CadastralParcel {

  val zoneType: ZoneType //TODO Does Road also have this type ? 

  val features : Information
  // val pieces_of_land : List[Patch] cut land into small pieces, with different features
  // val pieces_of_land : List[List[Patch]] same but in 3d 

  // polygon: List[Point]

  val buildings : List[Building] // Building type TBD

  /** BFS search over neighbors parcelles
  * @return first parcelle of type Road found 
  */
  def findNearestRoad(): Road = {

  }
}

class Information {

  // main features
  val area : Double // m^2
  // polygon: List[Point]
  
  // This attributs contains information about the land, extend them with what we need/is available
  val geological_info: Geological
  val ground_info: Ground
  val crops : Crops 

class Geological {
  // examples
  groundwater_levels : Double // m 
  heat_flux_density : Double // mW/m^2 
  rock_density: Double // kg/m^3
  temperature_at_100m : Double 
  //...
}

class Ground {
  val steepness : Int  
  val altitude : Int 
  val annual_rainfall : Double // mm/year
  val water_retention_capacity : Double // 1 == 1 mm of water accesible for plants at 1 cm below the ground
  val permeability : Double // coefficient 
  val depth_soil_exploitable_roots : Double
  //...
}

class Crops {
  val type : CropsType
  //Impact on land and productivity
  val fertilizer : List[Fertilizer]
  val pesticide : List[Pesticide]
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

class Building{

  //val type: page 13 vd.ch/fileadmin/user_upload/organisation/dinf/sit/fichiers_pdf/Merkmalkatalog_V_3-6_F.pdf  
  val surface : Double 
  val coordinates: (Int, Int)  //p.24 vd.ch/fileadmin/user_upload/organisation/dinf/sit/fichiers_pdf/Merkmalkatalog_V_3-6_F.pdf 
  val status // p.27
  //... (year of construction, ....)
}


//This object is in charge of find a path between 2 parcelles
object PathFinder {

  val roads : List[Road]
  val road_intersection : List[Intersection] //choice between multiple roads
  val rails : List[Railway]
  val marshalling_yard : List[Yard] // Only yard atm, but can be industrial complex also (+ other ?) 

  val roadGraph: scalax.collection.Graph
  val railGraph: scalax.collection.Graph

  def createRoadGraph(): scalax.collection.Graph = {
  }
                  
  def createRailGraph(): scalax.collection.Graph = {
  }

  /** 
  * Remove on graph prohibited roads for vehicle and find path using the following logic:
  * Find path to reach cantonal road from start_parcelle
  * Advance near destination on cantonal roads 
  * Exit cantonal roads and finish on communal roads until end_parcelle
  * First version, ofc need more work on it 
  * @return all intersections of the path (used to compute time travel + distance)
  */
  def findPath(start_parcelle: CadastralParcel, end_parcelle: CadastralParcel, vehicle_type: Any /** TBD */): List[Intersection] = {
    val starting_road: Road = start_parcelle.findNearestRoad()
    val ending_road: Road = end_parcelle.findNearestRoad()
  }

}





https://www.etat.ge.ch/geoportail/pro/?portalresources=CAD_PARCELLE_MENSU

https://www.geo.vd.ch/?&mapresources=GEOVD_DONNEESCADASTRALES,GEOVD_DONNEESBASE contains parcells and dp 

//routes: DP communal, DP cantonal






PATH FINDER 

algo pour trouver path: 
On part d'une adresse, si DP (cantonal ou departemental) dans la list des voisins, alors on a la première route, et on la suit jusqu'a destination
Sinon on fait un algo type BFS/DFS, qui check si un node est voisin ou est une DP 
Une fois sur la road, enchainement de DP ou parcelle privée si fin de DP, until arrive to the destination

Une fois le trajet complété, cache le path dans l'agent qui effectu le trajet.

Compute the length of roads:

How to get the shortest path, or at least decent ones ? Need a graph representation ? 
-> consider intersection as node, and roads as edges
Use the idea that when you travel the classic path is: 
- smaller road into bigger ones
- travell on bigger one 
- bigger one to small one to arrive at destination 
A* algorithm 


Fonctionnement global: 
Un graph qui represent l'ensemble des roads et intersection. Les edges doivent influencé le choix
 i.e maybe go as fast as possible on a highway, and exit near to the destination. 
 Par rapport aux parcelles, les roads présentes sur des parcelles privés ne peuvent en théorie pas être emprunté 
 par celui qui se déplace. A PART au début -> c'est pour ca qu'au début on fait un BFS/DFS sur les voisins pour trouver la
 route la plus proche.

 Representé route par des "parcelles" utile pour calculer la distance entre 2 intersections, et du coup 
 remplir le graph 

 Algo :
1. on trouve le chemin non privé le plus proche (de proche en proche à travers les parcells) pour le départ
 et l'arrivé. 
2. On se sert du graph pour joindre les 2 verticies(correspondant aux vertices auquels 
les routes de départ aboutissent)


Pour les parcelles "paumé", utiliser l'étape 1 plus longtemps 


We can also transport with train: 

- find optimal path in train. FInd the start point and end point (where stuff is loaded on train)
- apply algo above to find roads from farm or whatever to start point, and from end point to final destination



// TODO !!!! No infos exists on size of private roads....