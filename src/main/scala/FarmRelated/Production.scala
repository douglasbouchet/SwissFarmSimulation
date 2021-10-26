package FarmRelated
import Owner.Owner
import Securities.Commodities.Commodity
import Simulation.Simulation
import modifyFromKoch.Person

/**
 * We assume that before creating the Production, the owner has already bought the consumed commodities
 * (If the consumed quantities are not filled, efficiency is reduced -> production is lower)
 * @param s
 * @param owner
 * @param nEmployee
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

  var costsConsumables: Double = 0.0
  var frac : Double = 1.0
  frac = computeFrac
  var endProductionTimer: Int = s.timer + timeToComplete

  //Constructor
  {
    //hire the employee
    for (_ <- 1 to nEmployee) {
      if (s.labour_market.nonEmpty)
        employees.push(s.labour_market.pop.asInstanceOf[Person])
      else
        println("Lacking of people to hire")
    }
  }

  /** Pay the salary, the cost is added to the production cost  */
  def payWorkers(): Unit = {
    for(a <- employees) owner.transfer_money_to(a, salary)
    //TODO does the salary needs to be added inside production cost ? might say yes
  }


  /** Compute the efficiency of the production (1.0 == max, 0.0 == null production)
   * based on the ratio of available vs consumed commodities */
  def computeFrac: Double = {
    for(x <- consumed) {
      val n = math.min(owner.available(x._1), x._2) // requested and available
      costsConsumables += owner.destroy(x._1, n)
      frac = math.min(frac, n.toDouble / x._2)
    }
    frac
  }

  /** This method is called at the end of the production (i.e when time to complete is reached)
   * For each commodity produced:
   *  compute its unit cost
   *  add the produced quantity inside owner inventory
   * */
  def computeProduction(): Unit = {
    produced.foreach{
      case(com: Commodity, quantity: Int) =>
        val unitsProduced = (quantity  * frac).toInt
        //assume each employee splits its working time evenly on each commodity
        val personnelCosts = employees.length * salary * timeToComplete / produced.length
        val totalCost : Double = costsConsumables + personnelCosts
        val unitCost = totalCost / unitsProduced
        if(unitsProduced > 0) owner.make(com, unitsProduced, unitCost)
    }
  }

  //
  def getProduction: Boolean = {
    if(s.timer >= endProductionTimer){
      computeProduction()
      die()
      true
    }
    else false
  }


  def die(): Unit = {
    employees.foreach(s.labour_market.push(_))
    employees.clear()
  }


}
