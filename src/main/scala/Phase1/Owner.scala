package owner{

  import `enum`.Goods.Goods
class Owner(){

  protected var inventory : collection.mutable.Map[Goods, Int] = collection.mutable.Map[Goods, Int]()

  /** This is the number of units in the inventory available for taking;
      if the position is shorted (negative), available returns 0.
   */
  def available(item: Goods) = math.max(0, inventory.getOrElse(item, 0))

  def transfer_money_to(to: Owner, amount: Int) {
    to.capital += amount;
    capital -= amount;
  }

  /** Sell strictly number of units, short selling possible. */
  def atomic_sell_to(buyer: Owner, item: ITEM_T, units: Int,
                     unit_price: Double) {
    assert(units >= 0); // respect trading direction: it's a sell

    if(! inventory.contains(item)) init_inv(item);

    if(this == buyer)
      println("WARNING Owner.atomic_sell_to: " + this + " selling to himself!");
    //assert(this != buyer);
    // it's robust under selling to oneself, but such a sell is probably
    // a bug elsewhere.

    if(! GLOBAL.silent)
      println((this + " sells " + units + "*" + item + " to " + buyer +
        " at " + (unit_price/100).toInt) + "/unit");

    if(unit_price < inventory_avg_cost(item))
      println("WARNING: " + this + " is selling at a loss!");

    buyer.recalculate_inv_avg_cost(item, units, unit_price);
    recalculate_inv_avg_cost(item, -units, unit_price);

    // transfer asset
    buyer.inventory(item) += units;
    inventory(item) -= units;

    buyer.transfer_money_to(this, math.ceil(units * unit_price).toInt);

    // println("Now buyer = " + buyer + " and seller = " + this);
  }

  /** No shorting: sell no more than inventory. */
  def partial_sell_to(buyer: Owner, item: ITEM_T, units: Int,
                      unit_price: Double) : Int = {
    val available = math.max(inventory(item), 0);
    val n = math.min(available, units); // no shorting

    atomic_sell_to(buyer, item, n, unit_price);

    n // return #units sold
  }

  /** Doesn't touch capital:
      assumes cost is already accounted for (paid for earlier).
   */
  final def make(item: ITEM_T, units: Int, unit_cost: Double) {
    assert(units > 0);
    if(! inventory.contains(item)) init_inv(item);
    recalculate_inv_avg_cost(item, units, unit_cost);

    inventory(item) += units;
  }
  /** Consumes items, which get removed from the inventory and their
      cost gets added to total_value_destroyed.
   */
  final def destroy(item: ITEM_T, units: Int) : Double = {
    if(! inventory.contains(item)) init_inv(item);
    val value_destroyed = inventory_avg_cost(item) * units;
    total_value_destroyed += value_destroyed;
    inventory(item) -= units;

    value_destroyed // returns cost of destroyed stuff
  }
}

}