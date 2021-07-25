package DougSimulation{

  
  import Agents._
  
  class DougSimulation(initAgents: List[Agents]){
    val agents : List[Agents] = initAgents
    
    def passTurn(){
      agents.foreach(_.findSupplies())
      }
    
      }
}