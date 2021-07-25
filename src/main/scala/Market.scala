package Market{

  
  import Agents._
  import Goods._
  
  
  //Basic version, everyone can buy/sell to everyone, but this will need to change
  class Market(){
    var goods : scala.collection.mutable.Map[Goods,Int] = scala.collection.mutable.Map() // we do not store yet provider of the source but this will come after(in order to manage silos etc..)
    
    def init(){
      allGoods.foreach(elem => goods += (elem -> 0))
      }
      
      def newProduct(agents: List[Agents]){
        agents.foreach(agent => {
          agent.produced.foreach(product =>{
            goods(product._1) = goods.getOrElse(product._1, 0) + product._2
            if(product._2 > 0){
              println(product._2 + " " + product._1 + " are put on the market")
            } 
          })
        })
      }
              
              
}
}