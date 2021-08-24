import Simulation._
import landAdministrator.CadastralParcel
import code._

case class Farm(s: Simulation) extends MultipleActionsSim {

  var parcels: List[CadastralParcel] = List()
  var name = "ferme de douglas"

  def addParcels(newParcels: List[CadastralParcel]) {
    parcels :::= newParcels
  }

  def actions: List[(Instruction, Int)] = ???

  override def algo = __do(println("Je dis bonjour "))
}