/** Keep track of all commodities/other things produced by the simulation
  * Usefull when simulated thousands of peoples/farms
  * Milestone 1: 
  *   Observe the global emission of CO2
  *   Observe the quantity of commodities produced
  */
package glob

import Securities.Commodities.{Commodity, all_commodities}
import Simulation.{SimO, Simulation}
import code.{__wait, _}

import scala.collection.mutable


//Should it be an agent, in order to update itself, or we should update him inside the main
class Observator(s: Simulation) extends SimO(s) {

  //All in Kg
  var year_co2: Double = 0.0
  var year_methane: Double = 0.0
  var year_ammonia: Double = 0.0

  var total_co2: Double = 0.0
  var total_methane: Double = 0.0
  var total_ammonia: Double = 0.0


  var annualProduction: collection.mutable.Map[Commodity, Double] = collection.mutable.Map[Commodity, Double]()
  resetAnnualProduction


  def resetAnnualProduction: Unit = {
    all_commodities.foreach(com => annualProduction.put(com, 0.0))
  }

  def resetAnnualEmissions: Unit = {
    year_co2 = 0.0
    year_methane = 0.0
    year_ammonia = 0.0
  }

  def updateTotalEmissions: Unit = {
    total_co2 += year_co2
    total_methane += year_methane
    total_ammonia += year_ammonia
  }

  def stats {
    println(s"Annual emissions: \nCo2 emitted: $year_co2 kg \nMethane emitted: $year_methane kg \nAmmonia emitted: $year_ammonia kg")

    println("The annual productions are: " + annualProduction.toString())
  }

  def globalStats: Unit = {
    println(s"Total emissions: \nCo2 emitted: $year_co2 kg \nMethane emitted: $year_methane kg \nAmmonia emitted: $year_ammonia kg")
  }

  def updateAnnualProd(com: Commodity, quantity: Double): Unit = {
    annualProduction.update(com , annualProduction.getOrElse(com, 0.0) + quantity)
  }

  override def mycopy(_shared: Simulation, _substitution: mutable.Map[SimO, SimO]): SimO = ???

  override protected def algo: Instruction = __forever(
    //Each new year, reset the annual production, and output the stats of the year
    __if(s.timer % 365*CONSTANTS.TICKS_TIMER_PER_DAY == 0){
      __do{
        updateTotalEmissions
        stats
        resetAnnualEmissions
        resetAnnualProduction
      }
    },
    __wait(1)
  )
}