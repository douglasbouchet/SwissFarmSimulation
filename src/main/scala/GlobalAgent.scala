package GlobalAgent

import Agents._


object GlobalAgent{

  var agents: List[Agents] = List()
  var map: GlobalMap = ??? // The map that represent the location of all agents, the lands and the roads as explain in the markdown



  //def findProduct(_from: Agent, product: Product) = ??? // This method can be called by every agent (GlobalAgent is an object (singleton + accessible by all))

}

class GlobalMap(){
  //TODO
}


//Maybe put it abstract and redefine more precise productf afterwards
case class Product(){
  //TODO
}