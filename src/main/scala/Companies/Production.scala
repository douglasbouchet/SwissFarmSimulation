package Companies

import Owner.{Owner, Seller}
import Securities.Commodities.Commodity
import Simulation.Simulation
import geography.{LandOverlay, LandOverlayPurpose}
import modifyFromKoch.Person

/**
 * We assume that before creating the Companies.Production, the owner has already bought the consumed commodities
 * (If the consumed quantities are not filled, efficiency is reduced -> production is lower)
 * @param s the main Simulation
 * @param owner the owner (produced and consumed commodities are put/taken from its inventory)
 * @param nEmployee
 * @param salary of each employee (assume pay 1 salary per epoch)
 * @param consumed
 * @param produced
 * @param employees Stack[Person] that will be paid each epoch
 */
class Production(
                s: Simulation,
                owner: Seller,
                nEmployee: Int,
                salary: Int,
                consumed: List[(Commodity, Int)],
                produced: List[(Commodity, Int)],
                timeToComplete: Int,
                employees: collection.mutable.Stack[Person] =
                collection.mutable.Stack[Person](),
                landOverlay: Option[LandOverlay] = None
                ){

  var costsConsumables: Double = 0.0
  var frac : Double = 1.0
  frac = computeFrac
  var endProductionTimer: Int = s.timer + timeToComplete
  val _produced: List[(Commodity, Int)] = produced
  var alive = true

  override def toString = "Production: " + produced.head._1 + " " + owner

  //Constructor
  {
    //hire the employee
    //TODO does not take into account that is some workers are missing, production line frac should decrease
    for (_ <- 1 to nEmployee) {
      if (s.labour_market.nonEmpty)
        employees.push(s.labour_market.pop.asInstanceOf[Person])
      else
        println("Lacking of people to hire")
    }
  }

  /** Pay the salary, the cost is added to the production cost
   * This could be call by the owner, at each epoch
   * */
  def payWorkers(): Unit = {
    for(a <- employees) owner.transfer_money_to(a, salary)
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
   *  - compute its unit cost (that will be used to compute the production cost)
   *  - add the produced quantity inside owner inventory
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

        println(this + "produced:" + com + " " + unitsProduced)
    }
  }

  /** fire all employee
   *  if some LandOverlay was given, set its purpose to LandOverlayPurpose.noPurpose (can be used for other Companies.Production/usage) */
  def die(): Unit = {
    employees.foreach(s.labour_market.push(_))
    employees.clear()
    alive = false
    //If some landOverlay were assigned to this production, save current purpose, and mark the new one as no purpose
    if(landOverlay.isDefined) {
      landOverlay.get.prevPurpose = landOverlay.get.purpose
      landOverlay.get.purpose = LandOverlayPurpose.noPurpose
    }
  }

  /** for all produced commodities, we add the owner as a seller */
  def addAsSeller(): Unit = {
    produced.foreach{
      case(com: Commodity, _ : Int) =>
        if(!s.market(com).sellers.contains(owner)){
          s.market(com).add_seller(owner)
        }

    }
  }

  /** Should be called each epoch by the owner:
   * Check if production has ended
   * If so, call computeProduction and die
   * @return false if the production has ended
   * */
  def getProduction: Boolean = {
    if(s.timer >= endProductionTimer && alive){
      die()
      if(frac > 0) {
        addAsSeller
        computeProduction()
      }
      return false
    }
    true
  }
}
