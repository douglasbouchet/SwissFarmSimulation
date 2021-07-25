import Agents._
import DougSimulation._

object MainExample {

  
  var herd = for(cow <- 1 to 2) yield new Cows(false, 100);

  var cattleFarm = new CattleFarm(false, 1, herd.toList)
  
  var sim = new DougSimulation(herd.toList :+ cattleFarm)

  def main(argv: Array[String]) {
    
    //cattleFarm.updtateState
    sim.run(20)
    
    
    
    
    //herd.foreach(_.stat())
    //herd.foreach(_.findSupplies())
  }
}