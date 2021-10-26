package FarmRelated
import Owner.Owner
import Securities.Commodities.Commodity
import Simulation.Simulation
import modifyFromKoch.Person

import scala.collection.mutable

/**
 * We assume that before creating the Production, the owner has already bought the consumed commodities
 * (If the consumed quantities are not filled, efficiency is reduced -> production is lower)
 * @param s
 * @param owner
 * @param n_employee
 * @param salary
 * @param consumed
 * @param produced
 * @param employees
 */
class Production(
                s: Simulation,
                owner: Owner,
                nEmployee: Int,
                salary: Int,
                consumed: List[(Commodity, Int)],
                produced: List[(Commodity, Int)],
                timeToComplete: Int,
                employees: collection.mutable.Stack[Person] =
                collection.mutable.Stack[Person]()
                ){

  var costs_consumables: Double = 0.0
  var frac : Double = 1.0
  var counter: Int = 0

  //Constructor
  {
    //hire the employee
    for (_ <- 1 to nEmployee) {
      if (s.labour_market.length > 0)
        employees.push(s.labour_market.pop.asInstanceOf[Person])
      else
        println("Lacking of people to hire")
    }

    frac = computeFrac

    //destroy de l'inventaire du owner les consumed
  }

  /** Pay the salary, the cost is added to the production cost  */
  def payWorkers: Unit = {
    for(a <- employees) owner.transfer_money_to(a, salary)
    //TODO does the salary needs to be added inside production cost ? might say yes
  }


  /** Compute the efficiency of the production (1.0 == max, 0.0 == null production)
   * based on the ratio of available vs consumed commodites */
  def computeFrac: Double = {
    for(x <- consumed) {
      val n = math.min(owner.available(x._1), x._2); // requested and available
      costs_consumables += owner.destroy(x._1, n);
      frac = math.min(frac, n.toDouble / x._2);
    }
    frac
  }

  /** This method sho */
  def computeProduction: Unit = {
    produced.foreach{
      case(com: Commodity, quantity: Int) =>
        val unitsProduced = (quantity  * frac).toInt
        //assume each employee splits its working time evenly on each commodity
        val personnelCosts = employees.length * salary * timeToComplete / produced.length
        val totalCost : Double = costs_consumables + personnelCosts
        val unitCost = totalCost / unitsProduced
        if(unitsProduced > 0) owner.make(com, unitsProduced, unitCost)
    }
  }

  //TODO
  def getProduction: Unit = {
    if(counter >= timeToComplete) {
      computeProduction
      die
    }
    else{
      counter +=
    }
  }


  def die: Unit = {
    employees.foreach(s.labour_market.push(_))
    employees.clear()
  }


}
