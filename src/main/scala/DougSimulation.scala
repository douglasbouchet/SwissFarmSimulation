package DougSimulation{

  
  import Agents._
  
  class DougSimulation(){

    var agents : List[Agents] = List()
    
    def init(agentList: List[Agents]){
      agentList.foreach(addAgent(_))
    }

    def addAgent(agent: Agents) {
      agents = agents :+ agent
    }

    def remAgent(agent: Agents) {
      agents = agents.filterNot(_ == agent)
    }

    def passTurn(){
      agents.foreach(_.updtateState())
      }
    
      
    def run(turn: Int){
      var i : Int = 0
      for (i <- 0 to turn){
        passTurn()
      }
    }
  }

  class Market(initAgents: List[Agents]){
    //TODO
  }
}