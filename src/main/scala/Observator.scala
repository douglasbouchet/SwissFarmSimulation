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
import FarmRelated.Farmer

import scala.collection.mutable


//Should it be an agent, in order to update itself, or we should update him inside the main
class Observator(s: Simulation, _farms: List[Farmer]) extends SimO(s) {

  //All in Kg
  var year_co2: Double = 0.0
  var year_methane: Double = 0.0
  var year_ammonia: Double = 0.0

  var total_co2: Double = 0.0
  var total_methane: Double = 0.0
  var total_ammonia: Double = 0.0

  var farms: List[Farmer] = _farms


  var annualProduction: collection.mutable.Map[Commodity, Double] = collection.mutable.Map[Commodity, Double]()
  resetAnnualProduction

  //For each commodity, store a tuple representing total inventory cost, and a counter to then compute the inventory cost mean
  var annualProductionCost: collection.mutable.Map[Commodity, Double] = collection.mutable.Map[Commodity, Double]()
  resetAnnualProductionCost

  def resetAnnualProduction: Unit = {
    all_commodities.foreach(com => annualProduction.put(com, 0.0))
  }

  def resetAnnualProductionCost: Unit = {
    all_commodities.foreach(com => annualProductionCost.put(com, 0.0))
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

  def updateAnnualProd(com: Commodity, quantity: Double): Unit = {
    annualProduction.update(com , annualProduction.getOrElse(com, 0.0) + quantity)
  }

  def stats {
    println(s"Annual emissions: \nCo2 emitted: $year_co2 kg \nMethane emitted: $year_methane kg \nAmmonia emitted: $year_ammonia kg")

    println("The annual productions are: " + annualProduction.toString())

    printAvgPrice
  }

  def globalStats: Unit = {
    println(s"Total emissions: \nCo2 emitted: $year_co2 kg \nMethane emitted: $year_methane kg \nAmmonia emitted: $year_ammonia kg")
  }

  def getProdCostFromFarm(): Unit = {
    all_commodities.foreach(com => {
      var totalCost = 0.0
      var n = 0
      farms.foreach(farm => {
        val cost = farm.sendInvCost(com)
        if(cost != 0.0) {
          totalCost += cost
          n += 1
        }
      })
      annualProductionCost.put(com, totalCost/(100 * n)) // /100 because price are computed x 100 (I guess from owner's code)
    })
  }

  def printAvgPrice(): Unit = {
    annualProductionCost.foreach{
      case(com: Commodity, totalCost: Double) => {
        println("Average price for " + com + ": " + totalCost)
      }
    }
  }

  override def mycopy(_shared: Simulation, _substitution: mutable.Map[SimO, SimO]): SimO = ???

  override protected def algo: Instruction = __forever(
    //Each new year, reset the annual production, and output the stats of the year
    __wait(365*CONSTANTS.TICKS_TIMER_PER_DAY),
    __do{
      updateTotalEmissions
      getProdCostFromFarm
      stats
      resetAnnualEmissions
      resetAnnualProduction
    }


  )
}