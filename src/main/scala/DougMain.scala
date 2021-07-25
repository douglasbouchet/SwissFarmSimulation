import Agents._
import DougSimulation._
import cowState._

object MainExample {

  
  var sim = new DougSimulation()
  
  var herd = for(cow <- 1 to 1) yield new Cow(sim,false, 100);
  
  var cattleFarm = new CattleFarm(sim, false, 1, herd.toList)

  var agentList : List[Agents] = herd.toList :+ cattleFarm



  def main(argv: Array[String]) {

    sim.init(agentList)

    sim.run(1000)

  }
}