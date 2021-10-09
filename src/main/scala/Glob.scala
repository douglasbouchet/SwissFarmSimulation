import Securities.Commodities._
// This object will be here to allow for putting for example co2 emission, get prices, get lands,...
//some global stuff
package glob {
import generator.Generator
import geography.{LandAdministrator, RoadNetwork}
import market.Prices

  object GLOB {

    val landAdministrator = new LandAdministrator("Glaris")
    val generator = new Generator
    val globalRoadNetwork: RoadNetwork = generator.generateRoadNetwork()
    /** This will update all "Global" classes instances, such as LandAdministrator, Prices (for the moment)
     */
    def update(): Unit = {
      landAdministrator.update()
    }
  }

}

package object CONSTANTS {

  val TICKS_TIMER_PER_DAY: Int = 1 //Change this to change the timer rate

  val WHEAT_SEEDS_PER_HA: Double = 150 // in kg per ha
  val WHEAT_PRODUCED_PER_HA: Double = 6000 // in kg per ha
  val KG_GRASS_PER_PADDOCK_HA: Int = 1300
  val HA_PER_WORKER: Int = 20
  val CONVERSION_WHEAT_FLOUR: Double = 0.8
  val TIME_FOR_PADDOCK_TO_RECOVER_GRASS = 30 * TICKS_TIMER_PER_DAY

  val KG_CO2_PER_WHEAT_CROP_HA: Double = 1900
  val KG_METHANE_COW_DAY: Double = 0.3
  val KG_AMMONIA_COW_DAY: Double = 0.005

  val KG_OF_BEEF_PER_MEATCOW: Int = 250
  val KG_OF_GRASS_PER_COW_DAY: Int = 18
  
  var workercounter: Int = 0
  
  //If timer tick = 1 per month, last 2 month. If 2x faster TICKS_TIMER_PER_MONTH -> Still last 2 month (but requires 4 ticks)
  val WHEAT_EXPIRY_TIMER_IN_MONTH = 2 * 30 * TICKS_TIMER_PER_DAY
  val FERTILIZER_EXPIRY_TIMER_IN_MONTH = 6 * 30 * TICKS_TIMER_PER_DAY


  val WHEAT_PROD_DURATION: Int = 364 * TICKS_TIMER_PER_DAY
  val FERTILIZER_PROD_DURATION: Int = 30 * TICKS_TIMER_PER_DAY
  val MEATCOW_PROD_DURATION: Int = 364 * 3 * TICKS_TIMER_PER_DAY // assume 3 years before a meat cow can be killed

  val CROP_PROD_DURATION: collection.mutable.Map[Commodity, Int] = scala.collection.mutable.Map[Commodity, Int](
    Wheat -> 365 * TICKS_TIMER_PER_DAY,//TODO check real vallues
    Pea -> 365 * TICKS_TIMER_PER_DAY,//TODO check real vallues
    CanolaOil -> 365 * TICKS_TIMER_PER_DAY//TODO check real vallues
  )

  val CROP_EFFICIENCY: collection.mutable.Map[Commodity, Int] = scala.collection.mutable.Map[Commodity, Int](
    Wheat -> 6000,
    Pea -> 4000,//TODO check real vallues
    CanolaOil -> 5000//TODO check real vallues
  )

  val SEEDS_PER_HA: collection.mutable.Map[Commodity, Int] = scala.collection.mutable.Map[Commodity, Int](
    Wheat -> 150,
    Pea -> 200,//TODO check real vallues
    CanolaOil -> 100//TODO check real vallues
  )

}





