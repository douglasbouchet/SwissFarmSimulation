package market

import Securities.Commodities._
import Simulation.Simulation

import scala.collection.mutable
/**
  * Give the base price of commodities
  * First milestone, only use a sinus to represent fluctuation.
  * Second milestone: use demand and production based on the SellersMarket to give base price
 * Used to get and update both domestic and world Prices
 * Milestone 1: Domestic and World prices are quasi equals
 * Milestone 2: They are interdependant, and not anymore just Oscillator, but also based on production and consumption
 * Current Ms1
  * @param markets
  */
  class Prices(s: Simulation) extends Oscillator(s){

    class DomesticPrices(s: Simulation) extends Oscillator(s){
      baseComValue = scala.collection.mutable.Map[Commodity, Double](
        WheatSeeds -> 100.0, //No Idea
        
        Wheat -> 240.0,
        Flour -> 280.0, //No Idea
        FeedStuff -> 300.0, //No Idea
        Fertilizer -> 80.0 //No Idea
      ) //constant
      trueComValue = mutable.Map[Commodity, Double](
        WheatSeeds -> 0.0, //No Idea
        Wheat -> 0.0,
        Flour -> 0.0, //No Idea
        FeedStuff -> 0.0, //No Idea
        Fertilizer -> 0.0 //No Idea
      ) //constant
    }

    class WorldPrices(s: Simulation) extends Oscillator(s){
      baseComValue = scala.collection.mutable.Map[Commodity, Double](
        WheatSeeds -> 100.0, //No Idea
        Wheat -> 240.0,
        Flour -> 280.0, //No Idea
        FeedStuff -> 300.0, //No Idea
        Fertilizer -> 80.0 //No Idea
      ) //constant
      trueComValue = mutable.Map[Commodity, Double](
        WheatSeeds -> 0.0, //No Idea
        Wheat -> 0.0,
        Flour -> 0.0, //No Idea
        FeedStuff -> 0.0, //No Idea
        Fertilizer -> 0.0 //No Idea
      ) //constant
    }

    val domesticPrices = new DomesticPrices(s)
    val worldPrices = new WorldPrices(s)

    def getDomesticPricesOf(com: Commodity): Double = {
      domesticPrices.getValueOf(com)
    }

    def getWorldPricesOf(com: Commodity): Double = {
     worldPrices.getValueOf(com)
    }


}



