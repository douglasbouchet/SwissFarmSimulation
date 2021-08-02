package owner 

import enum.Goods._

class Owner(){

  //Store all the goods owned by the trader
  protected var inventory : collection.mutable.Map[Goods, Int] = collection.mutable.Map[Goods, Int]()
  //TODO currently public, but pass in private after, just see how to modify both capital in a transfert
  var capital : Int 

  //def add some actions to perfom changes on capital and inventory (when buy/sell)
  //operations are made by trader/people 

  def available(item: Goods) = math.max(0, inventory.getOrElse(item, 0))

  final protected def init_inv(item: Goods) {
    inventory += (item -> 0);
  }

  final def make(item: Goods, units: Int) {
    assert(units > 0);
    if(! inventory.contains(item)) init_inv(item);
    inventory(item) += units;
  }

  final def destroy(item: Goods, units: Int) = {
    if(! inventory.contains(item)) init_inv(item);
    inventory(item) -= units;
  }
}