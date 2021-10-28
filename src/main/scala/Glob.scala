import Securities.Commodities._
import geography.LandOverlayPurpose._
import geography.LandOverlayPurpose

import scala.collection.mutable
// This object will be here to allow for putting for example co2 emission, get prices, get lands,...
//some global stuff

package object CONSTANTS {

  val TICKS_TIMER_PER_DAY: Int = 1 //Change this to change the timer rate

  val WHEAT_SEEDS_PER_HA: Int = 150 // in kg per ha
  val WHEAT_PRODUCED_PER_HA: Int = 6000 // in kg per ha
  val KG_GRASS_PER_PADDOCK_HA: Int = 1300
  val HA_PER_WORKER: Int = 20
  val CONVERSION_WHEAT_FLOUR: Double = 0.8
  val TIME_FOR_PADDOCK_TO_RECOVER_GRASS = 30 * TICKS_TIMER_PER_DAY

  val KG_CO2_PER_WHEAT_CROP_HA: Double = 1900
  val KG_METHANE_COW_DAY: Double = 0.3
  val KG_AMMONIA_COW_DAY: Double = 0.005

  val KG_OF_BEEF_PER_MEATCOW: Int = 250
  val KG_OF_GRASS_PER_COW_DAY: Int = 18

  val KG_FLOUR_FOR_1_BREAD: Double = 0.3
  
  var workercounter: Int = 0

  val M_SQUARE_PRICE: Int =  10 //francs

  val CHILD_TAKE_OVER_AGE: Int = 35 //constant for the moment, afterwards we can model child as a person and give differents starting ages

  //If timer tick = 1 per month, last 2 month. If 2x faster TICKS_TIMER_PER_MONTH -> Still last 2 month (but requires 4 ticks)
  val WHEAT_EXPIRY_TIMER_IN_MONTH = 2 * 30 * TICKS_TIMER_PER_DAY
  val FERTILIZER_EXPIRY_TIMER_IN_MONTH = 6 * 30 * TICKS_TIMER_PER_DAY



  val FERTILIZER_PROD_DURATION: Int = 30 * TICKS_TIMER_PER_DAY
  val MEATCOW_PROD_DURATION: Int = 364 * 3 * TICKS_TIMER_PER_DAY // assume 3 years before a meat cow can be killed
  val FLOUR_PROD_DURATION: Int = 5 * TICKS_TIMER_PER_DAY

  val ACTIVITIES_PROD_DURATION: collection.mutable.Map[LandOverlayPurpose, Int] = scala.collection.mutable.Map[LandOverlayPurpose, Int](
    wheatField -> 364 * TICKS_TIMER_PER_DAY,//TODO check real vallues
    paddock -> MEATCOW_PROD_DURATION, 
   // Pea -> 364 * TICKS_TIMER_PER_DAY,//TODO check real vallues
    //CanolaOil -> 364 * TICKS_TIMER_PER_DAY//TODO check real vallues
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

  //Map(LandPurpose -> (ls_consumed, ls_produced))
  //assume 10 cows per paddock atm
  val PROD_MAP: collection.mutable.Map[LandOverlayPurpose, (List[(Commodity, Int)], List[(Commodity, Int)])] = scala.collection.mutable.Map[LandOverlayPurpose, (List[(Commodity, Int)], List[(Commodity, Int)])](
    wheatField -> (List((WheatSeeds, WHEAT_SEEDS_PER_HA)), List((Wheat, WHEAT_PRODUCED_PER_HA))),
    paddock -> (List((Grass, KG_GRASS_PER_PADDOCK_HA)), List((Beef, KG_OF_BEEF_PER_MEATCOW))),
    noPurpose -> (List(), List())
    
  )

  val LAND_OVERLAY_PURPOSE_TO_COMMODITY: scala.collection.mutable.Map[LandOverlayPurpose, Commodity] = scala.collection.mutable.Map[LandOverlayPurpose, Commodity](
    wheatField -> Wheat,
    paddock -> Beef,
  )

  val COMMODITY_TO_LAND_OVERLAY_PURPOSE: scala.collection.mutable.Map[Commodity, LandOverlayPurpose] = scala.collection.mutable.Map[Commodity, LandOverlayPurpose](
     Wheat -> wheatField,
     Beef -> paddock,
  )

}





