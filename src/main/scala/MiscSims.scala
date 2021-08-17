package Simulation.SimLib
import code._
import Simulation._
import Simulation.Factory._
import Markets._
import Securities.Commodities._


class Source(commodity: Commodity, units: Int, p: Int,
             shared: Simulation) extends
  SimO(shared) with SimpleSim {
  {
    shared.market(commodity).add_seller(this);
    make(commodity, units, 0); // at no cost
  }

  def mycopy(_shared: Simulation,
             _substitution: collection.mutable.Map[SimO, SimO]) = {
    val n = new Source(commodity, units, p, _shared);
    copy_state_to(n);
    n
  }

  def action = __do{}
  override def price(dummy: Commodity) = Some(p)
}


case class Trader(commodity: Commodity,
                  desired_inventory: Int,
                  shared: Simulation) extends
  SimO(shared) with SimpleSim {
  {
    shared.market(commodity).add_seller(this);
  }

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

  def action = __do {
      if(available(commodity) < desired_inventory) {
        val missing = shared.market(commodity).
                        market_buy_order_now(shared.timer, this, 1);
      }
    }
}



// A regular buyer for a sandbox simulation
case class Buyer(commodity: Commodity,
                 units_per_tick: () => Int,
                 shared: Simulation) extends SimO(shared) with SimpleSim {

  def mycopy(_shared: Simulation,
             _substitution: collection.mutable.Map[SimO, SimO]) = {
    val n = Buyer(commodity, units_per_tick, _shared)
    copy_state_to(n);
    n
  }

  def action = __do {
      shared.market(commodity).
        market_buy_order_now(shared.timer, this, units_per_tick());
    }
}


 
class Farm(s: Simulation) extends Factory(
  ProductionLineSpec(1, List((Land, 1)), List(), (Wheat, 20), 4), s)


//Not working, just an example
def trivial_prod_function(cereals: List[SimCommodities]) : (SimCommodity, Int) {
  val saleable = cereals.filter(c => c.state == harvestable & c.weight > 1.5)
  return (Wheat, saleable.size)
}

//Really Basic
class Farm(s: Simulation) extends Factory(
  List(
    ProductionLineSpec(1, List((Land, 1)), List(WheatSeeds, 1000), for i in range(1000) yield Wheat(), trivial_prod_function, 10),
    ProductionLineSpec(1, List((Land, 1)), List(BarleySeeds, 500), for i in range(500) yield Barley(), trivial_prod_function, 9)
  /** parameters in ProductionLineSpec like number of wheat, could be obtained as a function
  of land's area */ 
)
{
  val fertilize: Action = {
    
  }
}

class Mill(s: Simulation) extends Factory(
  ProductionLineSpec(1, List(), List((Wheat, 10)), (Flour, 10), 1), s)

class Cinema(s: Simulation) extends Factory(
  ProductionLineSpec(2, List(), List(), (MovieTicket, 2000), 1), s)

class CattleFarm(s: Simulation) extends Factory(
  ProductionLineSpec(1, List((Land, 1)), List(), (Beef, 5), 6), s)

class McDonalds(s: Simulation) extends Factory(
  ProductionLineSpec(1, List(), List((Flour, 10), (Beef, 5)),
                 (Burger, 10), 2), s)



