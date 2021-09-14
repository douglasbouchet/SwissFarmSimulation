package prices
import Securities.Commodities._
import Markets._
/**
  * Give the base price of commodities
  * First milestone, only use a sinus to represent fluctuation.
  * Second milestone: use demand and production based on the SellersMarket to give base price
  * @param markets
  */
  class Prices(/**markets: scala.collection.mutable.Map[Commodity, SellersMarket]*/){

  //The base price for each commodity. In euros/Tons
  private val baseComPrices = scala.collection.mutable.Map[Commodity, Double](
    WheatSeeds -> 100.0, //No Idea 
    Wheat -> 240.0,
    Flour -> 280.0, //No Idea
    FeedStuff -> 300.0, //No Idea
    Fertilizer -> 80.0 //No Idea
  ) //constant
  private val comPrices = scala.collection.mutable.Map[Commodity, Double](
    WheatSeeds -> baseComPrices(WheatSeeds),
    Wheat -> baseComPrices(Wheat),
    Flour -> baseComPrices(Flour),
    FeedStuff -> baseComPrices(FeedStuff),
    Fertilizer -> baseComPrices(Fertilizer)
  ) //fluctuate

  val timeStep: Double = 2*Math.PI / (12 * 2 * CONSTANTS.TICKS_TIMER_PER_MONTH) // For a period of 2 years, if each timestep is 1 month
  var counter: Int = 0
  val rnd = scala.util.Random

  //Change it with a sinus of amplitude 20% of base price, + small jumps of [-3;3]
  def updatePrice(com: Commodity): Unit = {
    comPrices.update(com, 
    BigDecimal(baseComPrices(com)*(1 + 0.2*Math.sin(timeStep * counter)) -3 + rnd.nextInt(6)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
  }

  def updateAllPrices(): Unit = {
    comPrices.keySet.foreach(updatePrice)
    counter += 1
  }

  def getPriceOf(com: Commodity): Double = {
    comPrices(com)
    //comPrices(com) match {
    //  case Some(price) => price
    //  case None => {
    //    println("There is no current global price for: " + com)
    //    0.0
    //  }
    //}
  }

}