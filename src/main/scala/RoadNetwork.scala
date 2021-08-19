/** Milestone 1: Consider only undirected edges, with name, max weight & lenght*/

/** Milestone 2: Add directed edges, bridges, highways, railways */ 

package roadNetwork

import scalax.collection.Graph // or scalax.collection.mutable.Graph
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._

import scalax.collection.edge.WUnDiEdge
import scalax.collection.edge.Implicits._

import landAdministrator.CadastralParcel
import Owner._


class Node(val id: String){
  override def toString = s"$id "
}

/** Represent an intersection between multiples roads. A node in the graph
* We might not want to store in/out going edges, as it will be stored in the graph
Constructor's arguments depend on data, hard to know what is necessary at the moment */ 
case class Intersection(override val id: String) extends Node(id){}

/** The point where parcels are connected to the network 
TODO do we have to store connectedParcels, or maybe having this info only inside each parcel is sufficient ? */
case class ParcelAccess(override val id: String, var connectedParcels: List[CadastralParcel]) extends Node(id) {}

/** weight of edges could be computing as the length of road/avg speed */
case class EdgeRoad[+N](fromNode: N, toNode: N, name: String, roadWeight: Double, maxWeight: Int)
  extends WUnDiEdge[N](NodeProduct(fromNode, toNode), roadWeight)
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
  implicit final class ImplicitEdge[A <: Node](val e: WUnDiEdge[A]) extends AnyVal {
    def ##(name: String, roadWeight: Double, maxWeight: Int) = new EdgeRoad[A](e._1, e._2, name, roadWeight, maxWeight)
  } 
}

// val edge = laus ~> gen ## 40 does not seem to works, use EdgeRoad[Intersection](laus, gen, 40) instead

object RoadNetwork {

  var roadNetwork: scalax.collection.Graph[Node, EdgeRoad] = Graph()

  def addNode(node: Node) = roadNetwork += node
  def rmNode(node: Node) = roadNetwork -= node
  def addEdge(edge: EdgeRoad[Node]) = roadNetwork += edge
  def rmEdge(edge: EdgeRoad[Node]) = roadNetwork -= edge


  def n(outer: Node): g.NodeT = g get outer

  //small example
  val doug: Owner = new Owner()
  val parcelDoug = new CadastralParcel(("Morges", 110), doug, List(), 1)
  val papa: Owner = new Owner()
  val parcelPapa = new CadastralParcel(("Vaison", 1221), papa, List(), 1)
  val laus = Intersection("Lausanne")
  val gen = Intersection("Geneve")
  val start = ParcelAccess("chez moi", List(parcelDoug))
  val end = ParcelAccess("chez papa", List(parcelPapa))
  val h0 = EdgeRoad[Node](start, laus, "DP12", 2, 10)
  val h1 = EdgeRoad[Node](start, gen, "DP12", 1, 10)
  val h2 = EdgeRoad[Node](laus, end, "DP13", 2, 10)
  val h3 = EdgeRoad[Node](gen, end, "DP14", 10, 10)
  val g = Graph(h0,h1,h2,h3)
  val path = g.get(start) shortestPathTo g.get(end)
  val goodpath = path.get
  goodpath.weight
  goodpath.nodes

}






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

