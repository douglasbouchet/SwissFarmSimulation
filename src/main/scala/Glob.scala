
// This object will be here to allow for putting for example co2 emission, get prices, get lands,...
//some global stuff
package glob {
import landAdministrator._
import prices.Prices
  object GLOB {

    val observator = new Observator
    val landAdministrator = new LandAdministrator(0,0)
    val prices = new Prices(/**market*/)
    val ju = 1111
  }

}

package object CONSTANTS {

  val TICKS_TIMER_PER_DAY: Int = 1 //Change this to change the timer rate

  val WHEAT_SEEDS_PER_HA: Double = 150 // in kg per ha
  val WHEAT_PRODUCED_PER_HA: Double = 6000 // in kg per ha
  val KG_GRASS_PER_PADDOCK_HA: Int = 1300
  val HA_PER_WORKER: Int = 20
  val CONVERSION_WHEAT_FLOUR: Double = 0.8
  val KG_CO2_PER_WHEAT_CROP_HA: Double = 1900

  val KG_OF_BEEF_PER_MEATCOW: Int = 250
  val KG_OF_GRASS_PER_COW_DAY: Int = 18
  
  var workercounter: Int = 0
  
  //If timer tick = 1 per month, last 2 month. If 2x faster TICKS_TIMER_PER_MONTH -> Still last 2 month (but requires 4 ticks)
  val WHEAT_EXPIRY_TIMER_IN_MONTH = 2 * 30 * TICKS_TIMER_PER_DAY
  val FERTILIZER_EXPIRY_TIMER_IN_MONTH = 6 * 30 * TICKS_TIMER_PER_DAY


  val WHEAT_PROD_DURATION: Int = 365 * TICKS_TIMER_PER_DAY
  val FERTILIZER_PROD_DURATION: Int = 30 * TICKS_TIMER_PER_DAY
  val MEATCOW_PROD_DURATION: Int = 365 * 3 * TICKS_TIMER_PER_DAY // assume 3 years before a meat cow can be killed
}





