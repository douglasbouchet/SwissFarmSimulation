package market

import Securities.Commodities._
import Simulation.Simulation

import scala.collection.mutable
/**
  * Give the base price of commodities
  * First milestone, only use a sinus to represent fluctuation.
  * Second milestone: use demand and production based on the SellersMarket to give base price
  * @param markets
  */
  class Prices(s: Simulation) extends Oscillator(s){

  //The base price for each commodity. In euros/Tons
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

