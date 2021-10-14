/**
 * This abstract class should be extended for each type of company we want to create, between the farmers and the consumers
 * We use an abstract class to make it easier when  looking for a specific type of company in land Administrator
 */
package Companies

import Owner.Seller
import Securities.Commodities._
import Simulation.Simulation
//import _root_.Simulation.Factory.{Factory, ProductionLineSpec}
import modifyFromKoch.{Factory, ProductionLineSpec}
import code.{__do, __forever, __wait}
import geography.{CadastralParcel, LandAdministrator}

import scala.collection.mutable

/**
 * @param s the Simulation instance
 * @param _parcels Each company should be located somewhere (in order to find local suppliers and to be found).
 * @param lAdmin Used to find some local providers, sellers
 * @param pls The production Line spec of this company
 */
abstract class Company(s: Simulation, lAdmin: LandAdministrator, _parcels: List[CadastralParcel], pls: ProductionLineSpec) extends Factory(pls, s){

  var parcels: List[CadastralParcel] = _parcels
  require(parcels.nonEmpty)
  _parcels.foreach(_.owner = this) //TODO check if correctly assigned

  //This will be used to decide which production to make in function of the last year demand, + benefits
  val lastYearDemand: mutable.Map[Commodity, Double]      = scala.collection.mutable.Map[Commodity, Double]()
  val lastYearBenefits: mutable.Map[Commodity, Double]    = scala.collection.mutable.Map[Commodity, Double]()
  val lastYearIncBenefits: mutable.Map[Commodity, Double] = scala.collection.mutable.Map[Commodity, Double]()

  /**
   * check inside contact network if we have seller for this commodity
   * If some, call market_buy_order_now with (some)
   * Else get the closest sellers for this type from the landAdministrator (until buy quantity is satisfied)
   * @param line (which commodity to buy, how many)
   * TODO this method is ugly atm, cause no real method to have a map of type of agents -> list of this agent inside land admin
   */
  def buy(line: (Commodity, Int)): Boolean = {
    val usualSellers = contactNetwork.contactsSellingCom(line._1)
    if(usualSellers.nonEmpty){
      s.market(line._1).market_buy_order_now(s.timer, this, line._2, usualSellers) == 0
    }
    //Look for some agents(near to this one) that could provide the asked supply
    else{
      var candidates = List[Seller]()
      line._1 match {
        case Wheat | Pea | CanolaOil =>
          lAdmin.findNClosestFarmers(parcels.head, 3) match {
            case Some(value) => candidates = value.map(_._1)
            case None =>
          }
        case Flour =>
          lAdmin.findNClosestMills(parcels.head, 3) match {
            case Some(value) => candidates = value.map(_._1)
            case None =>
          }
        case Bread =>
          lAdmin.findNClosestBakery(parcels.head, 3) match {
            case Some(value) => candidates = value.map(_._1)
            case None =>
          }
        case _ => println("Commodity research not implemented in Company.scala")
      }
      s.market(line._1).market_buy_order_now(s.timer, this, line._2, candidates) == 0
    }
  }

  //TODO check if correctly
  override def bulk_buy_missing(_l: List[(Commodity, Int)], multiplier: Int) : Boolean = {
    val l = _l.map(t => {
      // DANGER: if we have shorted his position, this amount is
      // not sufficient.
      val amount = math.max(0, t._2 * multiplier - available(t._1))
      (t._1, amount)
    })
    l.forall(buy)
  }

  override protected def algo = __forever(
    __do {
      for(i <- (pl.length + 1) to goal_num_pl)
        add_production_line();
      for(i <- (goal_num_pl + 1) to pl.length)
        remove_production_line();

      // TODO: buy more to get better prices?
      //println("Factory.algo: this=" + this);
      val still_missing = bulk_buy_missing(pls.consumed, pl.length);
    },
    __wait(1),
    __do {
      assert(hr.employees.length == pl.length * pls.employees_needed);
      hr.pay_workers();
    }
  )
}


