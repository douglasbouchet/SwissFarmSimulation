/** Milestone 1: Consider only undirected edges, with name, max weight & lenght
  */

/** Milestone 2: Add directed edges, bridges, highways, railways */

package roadNetwork

import scalax.collection.Graph
import scalax.collection.mutable.Graph
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._

import scalax.collection.edge.WUnDiEdge
import scalax.collection.edge.Implicits._
import scalax.collection.GraphTraversal._

import landAdministrator.CadastralParcel
import Owner._

class Intersection(val id: String) {
  override def toString = s"$id "
}

/** Represent an intersection between multiples roads. A node in the graph We
  * might not want to store in/out going edges, as it will be stored in the
  * graph Constructor's arguments depend on data, hard to know what is necessary
  * at the moment
  */
//case class Intersection(override val id: String) extends Node(id){}

/** The point where parcels are connected to the network TODO do we have to
  * store connectedParcels, or maybe having this info only inside each parcel is
  * sufficient ?
  */
//case class ParcelAccess(override val id: String, var connectedParcels: List[CadastralParcel]) extends Node(id) {}

/** weight of edges could be computing as the length of road/avg speed */
case class EdgeRoad[+N](
    fromNode: N,
    toNode: N,
    name: String,
    roadWeight: Double,
    maxWeight: Int
) extends WUnDiEdge[N](NodeProduct(fromNode, toNode), roadWeight)
    with ExtendedKey[N]
    with EdgeCopy[EdgeRoad]
    with OuterEdge[N, EdgeRoad] {
  private def this(
      nodes: Product,
      name: String,
      roadWeight: Double,
      maxWeight: Int
  ) {
    this(
      nodes.productElement(0).asInstanceOf[N],
      nodes.productElement(1).asInstanceOf[N],
      name,
      roadWeight,
      maxWeight
    )
  }
  def keyAttributes = Seq(roadWeight)
  override def copy[NN](newNodes: Product) =
    new EdgeRoad[NN](newNodes, name, roadWeight, maxWeight)
  override protected def attributesToString = s" $name "
}
object EdgeRoad {
  implicit final class ImplicitEdge[A <: Intersection](val e: WUnDiEdge[A])
      extends AnyVal {
    def ##(name: String, roadWeight: Double, maxWeight: Int) =
      new EdgeRoad[A](e._1, e._2, name, roadWeight, maxWeight)
  }
}

// val edge = laus ~> gen ## 40 does not seem to works, use EdgeRoad[Intersection](laus, gen, 40) instead

/** We can make a class as it will be initialized in the main, before getting
  * called by "agents"
  */
class RoadNetwork(
    /** data: Any */
) /*roadData: Any */
/** Define type when data on road will be available
  */ {

  //val roadNetwork = scalax.collection.mutable.Graph(EdgeRoad[Node](new Node("a"),new Node("b"), "a", 1, 1))
  val roadNetwork: scalax.collection.mutable.Graph[Intersection, EdgeRoad] =
    scalax.collection.mutable.Graph[Intersection, EdgeRoad]()

  /** add(Node),add(EdgeRoad)m remove(EdgeRoad), remove(Node) already
    * implemented
    */

  def createGraph(
      /** data: Any */
  ) = {}

  /** Assume constant roadSpeed over all the road atm, afterwards change Double
    * -> List[(Double(distance), Int(speed))]
    */
  def CreateRoad(
      fromNode: Intersection,
      toNode: Intersection,
      name: String,
      roadLenght: Double,
      roadSpeed: Int,
      roadMaxWeight: Int
  ): Boolean = {
    val road = new EdgeRoad[Intersection](
      fromNode,
      toNode,
      name,
      roadLenght / roadSpeed,
      roadMaxWeight
    )
    roadNetwork.add(road) match {
      case true => true
      case false => {
        println("The road couldn't be added to the network"); false
      }
    }
  }

  def CreateNode(id: String): Boolean = {
    val node = new Intersection(id)
    roadNetwork.add(node) match {
      case true => true
      case false => {
        println("The node couldn't be added to the network"); false
      }
    }
  }

  def n(outer: Intersection): roadNetwork.NodeT = roadNetwork get outer

  /** TODO find how to define the type "Path" */
  def findPath(start: Intersection, end: Intersection)
  /** Define Type */
  = {
    n(start) shortestPathTo n(end) match {
      case Some(path) => path
      case None       => { println("No path was found"); null }
    }
  }

  /** TODO find how to define the type "Path" */
  def findPathWeightConstraint(
      start: Intersection,
      end: Intersection,
      weightConstraint: Int
  )
  /** Define Type */
  = {
    n(start).withSubgraph(edges =
      _.maxWeight > weightConstraint
    ) shortestPathTo n(end) match {
      case Some(path) => path
      case None => {
        println("No path was found"); null

        /** return Empty "Path" instead */
      }
    }
  }

  // def pathWeight(path: Any /** TODO define with "Path" type */): Double = path.weight

  //small example
  //val doug: Owner = new Owner()
  //val parcelDoug = new CadastralParcel(("Morges", 110), doug, List(), 1)
  //val papa: Owner = new Owner()
  //val parcelPapa = new CadastralParcel(("Vaison", 1221), papa, List(), 1)
  //val laus = Intersection("Lausanne")
  //val gen = Intersection("Geneve")
  //val start = ParcelAccess("chez moi", List(parcelDoug))
  //val end = ParcelAccess("chez papa", List(parcelPapa))
  //val h0 = EdgeRoad[Node](start, laus, "DP12", 2, 10)
  //val h1 = EdgeRoad[Node](start, gen, "DP12", 1, 10)
  //val h2 = EdgeRoad[Node](laus, end, "DP13", 2, 10)
  //val h3 = EdgeRoad[Node](gen, end, "DP14", 10, 10)
  //val g : scalax.collection.mutable.Graph[Node, EdgeRoad] = scalax.collection.mutable.Graph[Node,EdgeRoad](h0,h1,h2,h3)
  //val path = g.get(start) shortestPathTo g.get(end)
  //val goodpath = path.get
  //goodpath.weight
  //goodpath.nodes
}
