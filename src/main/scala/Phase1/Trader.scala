package Trader{

  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global



  import Market._
  import enum.Goods._
  
  trait Trader{
    
    var balance : Balance 
    var localTrader : List[Trader] = List()
    var market: Market = Market()
    
    //Implement the following sequence with the code operations (__forever, etc)
    
    //Use to buy a product, choice between market and local buy can be done randomly:
    //Trivial example, change next
    //Can be overrided after to offer more complexity
    def buy(product: (Any, Int)): Future[Boolean] = {
      if(localTrader.isEmpty){
        buyOnMarket(market, product)
      }
      else{
        buyLocally(localTrader.head, product)
        //random choice between both, etc...
      }
    }

    def buyLocally(_localTrader: Trader, product: (Any, Int)): Future[Boolean] = {
      return Future{true}
    }
    def buyOnMarket(_market: Market, product: (Any, Int)): Future[Boolean] = {
      return Future{true}
    }

    //Same with sell, in case of sellOnMarket, the product may not be sell immediatly or may never by sell 
    def sell(product: (Any, Int)): Future[Boolean] = {
      if(localTrader.isEmpty){
        sellOnMarket(market, product)
      }
      else{
        sellLocally(localTrader.head, product)
        //random choice between both, etc...
      }
    }

    def sellLocally(_localTrader: Trader, product: (Any, Int)): Future[Boolean] = {
      return Future{true}
    }
    def sellOnMarket(_market: Market, product: (Any, Int)): Future[Boolean] = {
      return Future{true}
    }
    
    }

    trait Owner{

      
    }
    
    
  //case class Balance(_initMoney: Int, _initGoods: List[Map[Goods, Int]]){
  //temp version only for compiling, actual version above
  case class Balance(_initMoney: Int, _initGoods: List[Map[Goods, Int]]){
    
    //var goods : List[Map[Goods, Int]] = _initGoods
    //temp version only for compiling, actual version above
    var goods : List[Map[Goods, Int]] = _initGoods
    var money : Int = _initMoney
    
    //Called by the trader when he buy or sells some product
    //Use money to add good to goods
    def add() = ???
    //Remove good from goods to make money
    def remove() = ???
    
    }

}