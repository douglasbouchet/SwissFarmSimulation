package DougSimulation{

  
  import Agents._
  import Goods._
  import Market._
  
  class DougSimulation(){

    var agents : List[Agents] = List()
    var market = new Market();
    
    def init(agentList: List[Agents]){
      agentList.foreach(agent => agent.init())
      agentList.foreach(addAgent(_))
      market.init()

      stat()
    }

    def addAgent(agent: Agents) {
      agents = agents :+ agent
    }

    def remAgent(agent: Agents) {
      agents = agents.filterNot(_ == agent)
    }

    def passTurn(){

      market.newProduct(agents)
      
      //buy all needs etc 
      agents.foreach(agent => agent.findSupplies(market))

      // Then change the state if all needs are found 
      agents.foreach(_.updateState(market))

      }
      
    def run(turn: Int){

      println("Running the simulation for " + turn + " turns")
      var i : Int = 0
      for (i <- 0 to turn){
        passTurn()
      }

      agents.foreach(agent => agent.stat())
      market.stat()
      emittedPollution()
    }

    def stat(){
      println("The agents are : ")
      agents.foreach(agent => agent.stat())
      market.stat()
    }

    def emittedPollution() = {
      agents.foreach(agent => agent match{
        case x : CattleFarm => {
          if(x.grassLand){
            println("The grassLand CattleFarm produced: " + (x.cO2).round + " kg of CO2 per kg of meat")
          }
          else{
            println("The mixed agriculture system (import feedstuff) CattleFarm produced: " + (x.cO2).round + " kg of CO2 per kg of meat")
          }
        }
        case _ =>
      })
    }
  }

  
}