class Parcelle
{
  val id : (String, Int) // (commune name, n° inside commune), unique over switzerland

  val owner : String // obtained in land register, should be a name 
  val adjacent_parcelles: List[Parcelle]
}


//private roads? as used in algo, length might be negligeable (or for big private parcelles,
//start from middle of parcelle, and compute length to nearest DP road, but how to ? )
class Road extends Parcelle {

//The id is the same as a land (DP 34)

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

  val features : Patch
  // val pieces_of_land : List[Patch] cut land into small pieces, with different features
  // val pieces_of_land : List[List[Patch]] same but in 3d 

  // polygon: List[Point]

  val buildings : List[Building]
}

class Patch {

  // main features
  val area : Double // m^2
  // polygon: List[Point]
  
  // This attributs contains information about the land, extend them with what we need/is available
  val crops : Crops 
  val geological_info: Geological
  val ground_info: Ground

  stuff about water,...
}

class Land_Overlay {
  structure: List[Patch]
}

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

  def createRoadGraph(roads: List[Road],
                      road_intersection: List[Intersection]): scalax.collection.Graph = {

  }
                  
  def createRailGraph:(rails: List[Railway],
                       marshalling_yard : List[Yard]): scalax.collection.Graph = {

  }

  /** 
  * Remove on graph prohibited roads for vehicle and find path using the following logic:
  * Find path to reach cantonal road from start_parcelle
  * Advance near destination on cantonal roads 
  * Exit cantonal roads and finish on communal roads until end_parcelle
  * First version, ofc need more work on it 
  */
  def findPath(start_parcelle: Parcelle, end_parcelle: Parcelle, vehicle_type: Any /** TBD */): List[Road] = {

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