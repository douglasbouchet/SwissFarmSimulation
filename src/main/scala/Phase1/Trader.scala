package Trader{

  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global



  import Market._
  import enum.Goods._
  import owner._
  
  trait Trader{
    
    var owner: Owner 
    var localTrader : List[Trader] = List()
    var market: Market = Market()
    
    //Implement the following sequence with the code operations (__forever, etc)
    
    //Use to buy a product, choice between market and local buy can be done randomly:
    //Trivial example, change next
    //Can be overrided after to offer more complexity
    //The future represent the possibility that a supplie, good is currently not buyable/sellable, 
    //But could be in the following turns
    def buy(product: (Any, Int)): Future[Boolean] = {
      //or if local trader does not have the needed supplies
      if(localTrader.isEmpty){
        buyOnMarket(market, product)
        //perfom changes of capital in both traders + change of inventory
      }
      else{
        buyLocally(localTrader.head, product)
        //perfom changes of capital in both traders + change of inventory
        //random choice between both, or best price etc... + add fact that local are priviliged (entraide)
      }
    }
    

    //CARRRRE, SO OPERATIONS NEEDS TO BE ATOMIC
    def buyLocally(_localTrader: Trader, product: (Any, Int)): Future[Boolean] = ???
      //return true
    //}
    def buyOnMarket(_market: Market, product: (Any, Int)): Future[Boolean] = ???
    //  return Future{true}
    //}

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

    def sellLocally(_localTrader: Trader, product: (Any, Int)): Future[Boolean] = ???
    //  return true
    //}
    def sellOnMarket(_market: Market, product: (Any, Int)): Future[Boolean] = ???
    //  return Future{basicSupplier}
    //}
    
    //Some functions to perform an exchange, based on changing the capital and inventory of an other Trader
    def transferMoneyTo(other: Trader, amount: Int): Boolean = {
      //ofc add assets etc...
      this.owner.capital -= amount
      other.owner.capital += amount
      return true
    }

    //+ an other method for crediting this trader when selling to someone or negative amount ? 

    //used in the upper methods before calling a buy/sell operation
    def traderGotSupplies(supplies: List[(Goods, Int)], _trader : Trader) : Boolean = ???
    }
}