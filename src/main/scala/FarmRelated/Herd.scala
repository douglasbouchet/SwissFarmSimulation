package FarmRelated

import Securities.Commodities.Commodity
import Simulation.Factory.{ProductionLine, ProductionLineSpec}
import Simulation.Simulation
import farmpackage.Farm
import landAdministrator.LandOverlay
import landAdministrator.LandOverlayPurpose.paddock

/**
 *
 * @param owner
 * @param _landOverlay
 * @param nCows
 * @param salary
 */
class Herd(owner: Farm, _landOverlay: LandOverlay, nCows: Int, salary: Int /**AnimalType */){

  assert(_landOverlay.purpose == paddock)
  //This should change if there is no more grass on this paddock
  var landOverlay: LandOverlay = _landOverlay
  var cows: List[MeatCow] = List[MeatCow]()

  /**
   * each MeatCow is a productionLine and needs a productionLineSpec.
   * Clearly an employee can handle more than 1 cow, that's why we are gonna set 1 employee for the first animal of the herd,
   * and 0 employee for the other ones
   * For the moment, let's assume 1 employee per herd.
   * number of cows is not limited by paddock, but will influence speed of grass consumption,
   * and cows will need to move earlier to another paddock
   */
  def initHerd(): Unit = {
    assert(nCows > 1)
    val required: List[(Commodity, Int)] = List[(Commodity, Int)]() //TODO
    val consumed: List[(Commodity, Int)] = List[(Commodity, Int)]() //TODO
    val produced: (Commodity, Int) = ??? //TODO
    val timeToComplete: Int = ??? //TODO implement from constant
    val boosters: Option[List[(Commodity, Int, Double)]] = None //TODO add some afters ?
    var lineSpec: ProductionLineSpec =
      new ProductionLineSpec(1, required, consumed, produced, timeToComplete, boosters)
    cows ::= new MeatCow(owner.s, lineSpec, salary)
    //we already got our employee, next animals does not need anymore employee
    //TODO see if this does not change the first line spec
    lineSpec = new ProductionLineSpec(0, required, consumed, produced, timeToComplete, boosters)

    cows :::= (for (n <- 2 to nCows) yield new MeatCow(owner.s, lineSpec, 0)).toList
  }

  //This should be called each time there is no more grass on current landOverlay, and farmer do not want to buy feedstuff
  def changeOfPaddock(): Unit = {}




  class MeatCow(s: Simulation, pls: ProductionLineSpec, salary: Int) extends ProductionLine(pls, owner, salary, s.timer) {
  //include the fact that can eat the grass contains on the paddock
    //include the fact that methane + ammonia is produced everyturn
  }
}

