package DougSimulation{

  
  import Agents._
  
  class DougSimulation(initAgents: List[Agents]){

    val agents : List[Agents] = initAgents
    
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