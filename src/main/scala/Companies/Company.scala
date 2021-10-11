package Companies

import Owner.Owner
import Securities.Commodities._
import Simulation.Factory.{Factory, ProductionLineSpec}
import Simulation.Simulation
import geography.{CadastralParcel, LandAdministrator}

/**
 * This will represent all the agents involved in food supply chain, between the farmers and the consumers
 * @param pls
 * @param shared
 * @param _parcels
 * Milestone 1: Only a factory that has one type of production
 * Milestone 2: add multiple production line, thus we can make a strategy to decide which commodities and in which quantity to produce
 */
abstract class Company(s: Simulation,lAdmin: LandAdministrator, _parcels: List[CadastralParcel]) extends Owner{
  val parcels = _parcels
  require(!parcels.isEmpty)

  //This will be used to decide which production to make in function of the last year demand, + benefits
  val lastYearDemand = scala.collection.mutable.Map[Commodity, Double]()
  val lastYearBenefits = scala.collection.mutable.Map[Commodity, Double]()
  val lastYearIncBenefits = scala.collection.mutable.Map[Commodity, Double]()

  /**
   * check inside contact network if we have seller for this commodity
   * If some, call market_buy_order_now with (some)
   * Else get the closest sellers for this type from the landAdministrator (until buy quantity is satisfied)
   * @param com
   */
  def buy(com: Commodity, quantity: Int): Unit = {
    val usualSellers = contactNetwork.contactsSellingCom(com)
    if(!usualSellers.isEmpty){
      s.market(com).market_buy_order_now(s.timer, this, quantity, usualSellers)
    }
    else{
      var candidates = List()
      com match {
        case (Wheat || Pea || CanolaOil) => {
          lAdmin.findNClosestFarmers(parcels(0), 3) match {
            case Some(value) => candidates = value.map(_._1)
            case None =>
          }
        }
      }
      //val possibleNewSellers =
    }
  }
}
