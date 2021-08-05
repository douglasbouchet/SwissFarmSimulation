package Trader{

  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global



  import Market._
  import enum.Goods._
  import owner._
  
  trait Trader{
    
    var owner: Owner 
    var localTraders : List[Trader] = List()
    var marketList: List[Market] = List()
    

    //Implement the following sequence with the code operations (__forever, etc)
    
    //Use to buy a product, choice between market and local buy can be done randomly:
    //The future represent the possibility that a supplie, good is currently not buyable/sellable, 
    //But could be in the following turns
    def buy(product: (Any, Int)): Future[Boolean] = {
      //or if local trader does not have the needed supplies
      if(localTrader.isEmpty){
        buyOnMarket(marketList, product)
        //perfom changes of capital in both traders + change of inventory
      }
      else{
        buyLocally(localTrader.head, product)
        //perfom changes of capital in both traders + change of inventory
        //random choice between both, or best price etc... + add fact that local are priviliged (entraide)
      }
    }
    

    //CARRRRE, OPERATIONS NEEDS TO BE ATOMIC
    def buyLocally(_localTrader: Trader, product: (Any, Int)): Future[Boolean] = ???

    def buyOnMarket(_market: List[Market], product: (Any, Int)): Future[Boolean] = ???

    //Same with sell, in case of sellOnMarket, the product may not be sell immediatly or may never by sell 
    def sell(product: (Any, Int)): Future[Boolean] = {
      if(localTrader.isEmpty){
        sellOnMarket(marketList, product)
      }
      else{
        sellLocally(localTrader.head, product)
        //random choice between both, etc...
      }
    }

    def sellLocally(_localTrader: Trader, product: (Any, Int)): Future[Boolean] = ???

    def sellOnMarket(_market: List[Market], product: (Any, Int)): Future[Boolean] = ???

    //register if a market if agents have never been to this market,
    //add product that are buyable to other people.
    def putInMarket() = ???
    
    //Some functions to perform an exchange, based on changing the capital and inventory of an other Trader
    def transferMoneyTo(other: Trader, amount: Int): Boolean = {
      //ofc add assets etc...
      this.owner.capital -= amount
      other.owner.capital += amount
      return true
    }

    //+ an other method for crediting this trader when selling to someone or negative amount ? 

    //used in the upper methods before calling a buy/sell operation
    
    }
}