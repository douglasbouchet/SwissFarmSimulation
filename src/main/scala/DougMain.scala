import Agents._
import DougSimulation._

object MainExample {

  
  var herd = for(cow <- 1 to 5) yield new Cows(false, 100);
  
  var sim = new DougSimulation(herd.toList)

  def main(argv: Array[String]) {
    //herd.foreach(_.stat())
    //herd.foreach(_.findSupplies())
    sim.passTurn()
  }
}