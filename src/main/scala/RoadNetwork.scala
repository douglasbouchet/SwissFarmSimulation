import scalax.collection.Graph // or scalax.collection.mutable.Graph
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._

import scalax.collection.edge.WDiEdge
import scalax.collection.edge.WUnDiEdge
import scalax.collection.edge.Implicits._

object RoadNetwork {
  val g = Graph(1~2 % 4, 2~3 % 2, 1~>3 % 5, 1~5  % 3, 3~5 % 2, 3~4 % 1, 4~>4 % 1, 4~>5 % 0)
  def n(outer: Int): g.NodeT = g get outer
  val path = n(3) shortestPathTo n(1)
}

/** Milestone 1: Consider only undirected edge, but try implement Complex edge types, possessing max weight, in order
to include this */


/** Milestone 2: Add directed edges ? */ 

case class Intersection(val name: String){
  override def toString = s"$name "
}

/** weight of edges could be computing as the length of road/avg speed 
Also adding maxweight for each road 
Working !!!!!!!
*/
case class EdgeRoad[+N](fromIntersection: N, toIntersection: N, name: String, roadWeight: Double, maxWeight: Int)
  extends WUnDiEdge[N](NodeProduct(fromIntersection, toIntersection), roadWeight)
  with ExtendedKey[N]
  with EdgeCopy[EdgeRoad]
  with OuterEdge[N,EdgeRoad] {
  private def this(nodes: Product, name: String, roadWeight: Double, maxWeight: Int) {
    this(nodes.productElement(0).asInstanceOf[N],
         nodes.productElement(1).asInstanceOf[N], name, roadWeight, maxWeight)
  }
  def keyAttributes = Seq(roadWeight)
  override def copy[NN](newNodes: Product) = new EdgeRoad[NN](newNodes, name, roadWeight, maxWeight)
  //override protected def attributesToString = s" ($roadWeight) + max weight: + ($maxWeight)"
  override protected def attributesToString = s" $name " 
} 
object EdgeRoad {
  implicit final class ImplicitEdge[A <: Intersection](val e: WUnDiEdge[A]) extends AnyVal {
    def ##(name: String, roadWeight: Double, maxWeight: Int) = new EdgeRoad[A](e._1, e._2, name, roadWeight, maxWeight)
  } 
}

// val x = laus ~> gen ## 40 does not seem to works, use EdgeRoad[Intersection](laus, gen, 40) instead

// val laus = Intersection("Lausanne"), pareil avec geneve et paris, shortest path fonctionne
//g.get(laus) shortestPathTo g.get(paris)

// val p = n(laus).withSubgraph(edges = _.max_weight > 3) pathTo n(gen) CA MARCHE, il tej bien les max weight trop petits
// pour l'example !! define n() as g.get()






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
