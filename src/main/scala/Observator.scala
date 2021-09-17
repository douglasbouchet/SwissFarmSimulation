/** Keep track of all commodities/other things produced by the simulation
  * Usefull when simulated thousands of peoples/farms
  * Milestone 1: 
  *   Observe the global emission of CO2
  *   Observe the quantity of commodities produced
  */
package glob 

class Observator {

  //All in Kg
  var Co2: Double = 0.0
  var methane: Double = 0.0
  var ammonia: Double = 0.0

  def stats {
    println(s"Co2 emitted: $Co2 kg \nMethane emitted: $methane kg \nAmmonia emitted: $ammonia kg")
  }

}