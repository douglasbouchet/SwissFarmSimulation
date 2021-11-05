package modifyFromKoch

import Securities.Commodities.Commodity
import Simulation.{SimO, SimpleSim, Simulation}
import code.__do

case class Trader(commodity: Commodity,
                  desired_inventory: Int,
                  shared: Simulation) extends
  SimO(shared) with SimpleSim {
  {
    shared.market(commodity).add_seller(this);
  }

  override def toString: String = "Trader of " + commodity.name

  def mycopy(_shared: Simulation,
             _substitution: collection.mutable.Map[SimO, SimO]) = {
    val n = Trader(commodity, desired_inventory, _shared);
    copy_state_to(n);
    n
  }

  override def price(dummy: Commodity) = {
    if(available(commodity) > 0)
      Some(1.05 * inventory_avg_cost.getOrElse(commodity, 0.0))
    else None
  }

  def action: __do = __do {
    if(available(commodity) < desired_inventory) {
      val missing = shared.market(commodity).
        market_buy_order_now(shared.timer, this, desired_inventory - available(commodity));
    }
  }
}
