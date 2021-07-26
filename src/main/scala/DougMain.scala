import Agents._
import DougSimulation._
import cowState._

object MainExample {

  
  var sim = new DougSimulation()
  
  var herd0 = for(cow <- 1 to 1) yield new Cow(sim, 100);
  var herd1 = for(cow <- 1 to 1) yield new Cow(sim, 100);
  
  var grassLandCattleFarm = new CattleFarm(sim, true, 1, herd0.toList)
  var notGrassLandCattleFarm = new CattleFarm(sim, false, 1, herd1.toList)

  var agentList : List[Agents] = herd0.toList ++ herd1.toList :+ grassLandCattleFarm :+ notGrassLandCattleFarm



  def main(argv: Array[String]) {

    sim.init(agentList)

    sim.run(500)

  }
}