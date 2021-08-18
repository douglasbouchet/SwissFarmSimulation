//import scalax.collection.GraphPredef._
//import scalax.collection.GraphEdge._
//import scalax.collection.constrained.mutable.Graph
//import scalax.collection.constrained.constraints.Connected
 
//implicit val conf: Config = Connected
//
//val e = Graph(1 ~ 2, 3 ~ 4) // Graph()
//val g = Graph(1 ~ 2, 2 ~ 4) // Graph(1, 2, 4, 1~2, 2~4)
//g += 3                      // Graph(1, 2, 4, 1~2, 2~4)
//g += 2 ~ 3                  // Graph(1, 2, 3, 4, 1~2, 2~3, 2~4)


//object RoadNetwork {
//
//  val roads : List[Road]
//  val road_intersection : List[Intersection] //choice between multiple roads
//  val rails : List[Railway]
//  val marshalling_yard : List[Yard] // Only yard atm, but can be industrial complex also (+ other ?) 
//
//  val roadGraph: scalax.collection.Graph
//  val railGraph: scalax.collection.Graph
//
//  def createRoadGraph(): scalax.collection.Graph = {
//  }
//                  
//  def createRailGraph(): scalax.collection.Graph = {
//  }
//
//  /** 
//  * Remove on graph prohibited roads for vehicle and find path using the following logic:
//  * Find path to reach cantonal road from start_parcelle
//  * Advance near destination on cantonal roads 
//  * Exit cantonal roads and finish on communal roads until end_parcelle
//  * First version, ofc need more work on it 
//  * @return all intersections of the path (used to compute time travel + distance)
//  */
//  def findPath(start_parcelle: CadastralParcel, end_parcelle: CadastralParcel, vehicle_type: Any /** TBD */): List[Intersection] = {
//    val starting_road: Road = start_parcelle.findNearestRoad()
//    val ending_road: Road = end_parcelle.findNearestRoad()
//  }
//
//}
