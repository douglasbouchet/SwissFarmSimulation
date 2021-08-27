/** Keep track of all commodities/other things produced by the simulation
  * Usefull when simulated thousands of peoples/farms
  * Milestone 1: 
  *   Observe the global emission of CO2
  *   Observe the quantity of commodities produced
  */
package Simulation 

class Observator {

  var Co2: Double = 0.0 //in Kg 

  def stats {
    println("The final CO2 emission was: " + Co2)
  }

}