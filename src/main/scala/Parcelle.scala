class Parcelle
{
  val id : (String, Int) // (commune name, n° inside commune), unique over switzerland

  var owner : String // obtained in land register, should be a single name 
  val adjacent_parcelles: List[Parcelle]
  // var which_land_overlay_part_of : List[(LandOverlay, Int)]
  
  // (Land overlay -> percentage of parcelle in (0 to 1))
  var part_of: collection.mutable.Map[LandOverlay, Double] = collection.mutable.Map[LandOverlay, Double]()

  val area: Double
}

//group the parcelles which are physically the same field/paddoc/meadow, and belong to one or multiple owners
// store the type locally. 
class LandOverlay(aggregation: List[(Parcelle, Double)]) {

  /** (Parcelle, Percentage occupied on it (0 to 1)) */ 
  //TODO shpuld we use a map isntead ??
  var land_aggregation : List[(Parcelle, Double)] = aggregation
  /** How much percentage of the aggregated lands each owner possess (some fields have multiple owner) */

  var ownership_distribution : List[(Owner, Double)] = cmpt_ownership_distrib(land_aggregation)

  //If not compiling, put it above
  def cmpt_ownership_distrib(new_land_aggregation: List[(Parcelle, Double)]): List[(Owner, Double)] = {

    val total_area: Double = new_land_aggregation.foldLeft(0.0){(acc, tup) => acc + tup._1.area * tup._2}

    val owner_area_use: List[Owner, Double)] =
       new_land_aggregation.map(elem => (elem._1.owner, elem._1.area * elem._2))
    var use_per_owner = collection.mutable.Map[String, Int]()
    owner_area_use.foreach{
      tup => map += (tup._1 -> use_per_owner.getOrElse(tup._1,0.0) + tup._2)
    }
    use_per_owner.toList.map(tup => (tup._1, tup._2 / total_area))

  }

  def addParcelle(parcelle: Parcelle, proportion: Double) = {
      assert(! land_aggregation.contains((parcelle,proportion)))
      land_aggregation ::= (parcelle, proportion)
      ownership_distribution = cmpt_ownership_distrib(land_aggregation)
  }

  def removeParcelle(parcelle: Parcelle) = {
    parcelle.get(this) match {
      case Some(x) => {
        assert(land_aggregation.contains((parcelle, x)))
        land_aggregation.filterNot(_ == (parcelle, proportion))
      ownership_distribution = cmpt_ownership_distrib(land_aggregation)
      }
      case None => println("trying to remove a parcelle that is not part of this land overlay")
    }
  }

  def getParcelles() = land_aggregation.map(_._1).toList

  class Crop(aggregation: List[(Parcelle, Int)],
           owner_distrib: List[(Int, Owner)],
           usage: /**TODO */) extends LandOverlay(aggregation, owner_distrib)
           {  }
  class Paddoc(aggregation: List[(Parcelle, Int)],
               owner_distrib: List[(Int, Owner)],
               animals: List[Animals]) {}

  class Meadow(aggregation: List[(Parcelle, Int)],
               owner_distrib: List[(Int, Owner)],) {
    //TODO what information to keep inside }
  //TODO ou alors on a cette information dans un attribut de type enumerate
}

/** object LandOverlayManager */
/** object LandOverlayAdministrator {*/

what it does: keep track of all lands, all lands overlay 
if you want to find all crops for example, if you want to find paddoc for your cows

!! Find for example paddoc at a distance from a Cattle farm ()
At start: find Land overlay (or part of it )
Method to get all LandOverlay that could be bought (And each parcelle that could be bought in this one)
//This might be done on the market, no need to store this info here.
object LandAdministrator(parcelle_data: Object, land_overlay_data: Object) {
  
  /** Constructor */
  {
    /** Create all parcelles, given data
    Set Owner to ""
    */
    def createParcelles(data: Object): List[Parcelle] = ???

    /** 
    For each paddoc/field/meadow:
      Find The list of Parcelle part of and by how much percentage  
      In function of area of parcelles + perc of parcelle inside overlay + owner find 
      how much does each owner of overlay possess of it (usefull for computing crops income of grouped farmers)
    */
    def createLandsOverlays(data: Object): List[LandOverlay] = ???

  
    var all_parcelles: List[Parcelle] = createParcelles()
    var all_lands_overlays : List[LandOverlay] = List()
    
    /** Optional */
    val all_crops: List[LandOverlay] = all_lands_overlays.filter(l_overlay => l_overlay.type == Crop)
  } // end constructor

  /** Split a land overlay in multiple lands overlays. 
  * @param current: LandOverlay, the land overlay to split 
  * @param into: List[LandOverlay], the final lands overlays 
  * into must only contain Parcelles belonging to current, and each Parcelle can appear in one and only one 
  * LandOverlay
  */
  def SplitLandOverlay(current: LandOverlay, into: List[LandOverlay]): Boolean = {
    
    assert(all_lands_overlays.contains(current))

    into.foreach(assert(_.getParcelles.length > 0)))
    //Check if each Parcelle of into is actually in current
    assert(into.foreach(_.getParcelles().foreach(current.getParcelles().contains(_))))
    //Check if no parcelles appear multiple times
    assert(into.flatten.map(_._1).length == into.flatten.map(_._1).distinct.length)

    all_lands_overlays.remove(current)
    into.foreach(all_lands_overlays ::= _)
    
    return true
  }

  def MergeLandOverlay(to_merge: List[LandOverlay]): Boolean = {

    var new_aggregation: List[(Parcelle, Int)] = to_merge.flatten
    //Check if not adding multiple time the same Parcelle
    assert(new_aggregation.map(_._1).distinct.length == new_aggregation.map(_._1).length)
    var merged_land_overlay = new LandOverlay(new_aggregation, List())
    assert(to_merge.foreach(all_lands_overlays.contains(_)))
    to_merge.foreach(all_lands_overlays.remove(_))
    all_lands_overlays ::= merged_land_overlay

    return true
  }


  def changePurpose(changing: LandOverlay, newState: LandOverlay) = {
    if (changing.type != newState.type){ // see if Crop != Paddoc not sure
      var newLandOverlay
    } 
  }
  //new ownership, new parcelle,...
  def changeOrganisation()
}


//private roads? as used in algo, length might be negligeable (or for big private parcelles,
//start from middle of parcelle, and compute length to nearest DP road, but how to ? )
class Road extends Parcelle {

/** Second element a string now (e.g DP 234) */
override val id : (String, String)

val adjacent_road_dist: List[(Road, Double)] // List of (Road, distance(m) from center of this to Road) cf plan ipad 

//TODO Find how to compute length between middle of the road and other roads

//val type : {national, cantonal, communal} usefull ? 
val max_weight_allowed : Int // in kg 

//... futher: capacity, risk of snow, status (bloqued for repair,...)
}

class Railway extends Parcelle {

  //a railway is defined between 2 Network node cf https://map.geo.admin.ch/?lang=fr&selectedNode=&topic=inspire&layers=ch.kantone.cadastralwebmap-farbe,ch.swisstopo-vd.geometa-grundbuch,ch.are.bauzonen,ch.bafu.hydroweb-messstationen_grundwasserzustand,ch.bfs.volkszaehlung-gebaeudestatistik_gebaeude,ch.swisstopo.vec25-gebaeude,ch.bfs.gebaeude_wohnungs_register,ch.blw.bodeneignung-gruendigkeit,ch.blw.bodeneignung-kulturland,ch.blw.bewaesserungsbeduerftigkeit,ch.swisstopo.geologie-rohstoffe-salz_abbau_verarbeitung,ch.swisstopo.geologie-geotechnik-gk500-genese,ch.swisstopo.geologie-rohstoffe-industrieminerale,ch.swisstopo.geologie-geosites,ch.swisstopo.geologie-gesteinsdichte,ch.swisstopo.geologie-geophysik-geothermie,ch.blw.bodeneignung-wasserspeichervermoegen,ch.blw.bodeneignung-vernaessung,ch.blw.bodeneignung-kulturtyp,ch.blw.ursprungsbezeichnungen-pflanzen,ch.swisstopo.swisstlm3d-eisenbahnnetz,ch.bav.schienennetz,ch.astra.hauptstrassennetz,ch.astra.strassenverkehrszaehlung-uebergeordnet&bgLayer=ch.swisstopo.swissimage&E=2522854.13&N=1156967.82&zoom=11.039025467272955&catalogNodes=1,58,108,287,306,109&layers_opacity=1,0.75,0.6,1,1,1,1,0.75,0.75,0.75,1,0.75,1,1,0.75,1,0.75,0.75,0.75,0.75,1,1,0.75,1&layers_visibility=true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,true,false,false&layers_timestamp=,,,,2019,,,,,,,,,,,,,,,,,,,
  val startNode: Int 
  val endNode: Int
  val startKm: Double
  val endKm: Double
  val length : Double = endKm - startKm 
  // ... number of ways etc.. 
}

class Land extends Parcelle {

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

class Land_Overlay {

  political_parcelles: List[Parcelle]
  structure: List[Patch]
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
  def findPath(start_parcelle: Parcelle, end_parcelle: Parcelle, vehicle_type: Any /** TBD */): List[Intersection] = {
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