package contact

import Markets.MarketMatchingUtilities
import Owner.{Owner, SalesRecord, Seller}
import Securities.Commodities.Commodity
import Timeseries.LogList

/**
 * The Goal of this class is to reflect the fact that people have usual suppliers in real life.
 * When a Buyer wants some commodities, it checks the possible seller on the SellerMarket
 * It chose the best seller not only based on price of products, but also on the clientScore
 * (You may want to buy at higher price to your friends, but not to much so you still look at other prices)
 * It basically just contains a list of Seller, and a clientScore + some method to update this list
 */
class ContactNetwork {

  //TODO maybe change and contacts BY commodity

  /** Just store a list of Seller, together with their clientScore */
  var contacts = scala.collection.mutable.Set[(Seller, Int)]()

  def addContact(contact: Seller): Unit = {contacts += ((contact,1))}

  def removeContact(toRemove: Seller): Unit = {
    contacts = contacts.filterNot(_._1 == toRemove)
  }

  def getContactScore(contact: Seller): Int = {
    contacts.find(_._1 == contact) match {
      case Some((seller, score)) => score
      case None => 0
    }
  }

  /** automatically add the contact if does not exists yet */
  def increaseScore(contact: Seller): Unit = {
    val oldScore = getContactScore(contact)
    contacts -= ((contact,oldScore))
    contacts += ((contact, Math.min(oldScore + 1,10)))
  }

  /** remove the contact if its score reach -1 */
  def decreaseScore(contact: Seller): Unit = {
    val oldScore = getContactScore(contact)
    contacts -= ((contact,oldScore))
    if(oldScore  > 0) {
      contacts += ((contact, oldScore - 1))
    }
  }

  def stats: Unit = {
    println("Contacts are: " + contacts.toString())
  }
}


/** Used to buy/sell product from local seller/buyer (geographically close)
  * Different than the sellerMarket in the sense that not all suppliers of a specific products are present on it.
  * Members should be added if they are near. 
  * Relation are reflexive (if A has B in its LocalMarket, then B should also have A in its LocalMarket)
  */
class LocalMarket(o: Seller) extends MarketMatchingUtilities[Seller]{
//
//  val localTraders = scala.collection.mutable.Set[Seller]()
//  val forSaleCommodities = scala.collection.mutable.Map[Commodity, scala.collection.mutable.Set[Seller]]()
//
//  var order_history = new LogList[SalesRecord]
//
//  def addLocalTrader(trader: Seller): Unit = {
//    localTraders.add(trader)
//    //Reflexivity
//    trader.localMarket.localTraders.add(o)
//  }
//  def removeLocalTrader(toRemove: Seller): Unit = {
//    localTraders.remove(toRemove)
//    //Reflexivity
//    toRemove.localMarket.localTraders.remove(o)
//  }
//
//  /** Put this seller on the forSaleCommodities of each localTrader */
//  def putOnLocalMarket(commodity: Commodity): Unit = {
//    localTraders.foreach((trader: Seller) => {
//      val oldSet = trader.localMarket.forSaleCommodities.get(commodity) match {
//        case Some(set) => set
//        case None => scala.collection.mutable.Set[Seller]()
//      }
//      trader.localMarket.forSaleCommodities.update(commodity, oldSet + o)
//    })
//  }
//
//  def marketBuyOrderNow(time: Int, buyer: Owner, commodity: Commodity, units: Int) : Int = {
//    //println("SellersMarket.market_buy_order_now " + this);
//    val (left_over, l) = bestMatch(commodity, units, buyer);
//    //println("Buying " + units + " on market: " + l + " " + left_over);
//    val p = compute_price(l, commodity); // can't reorder this line and the next
//    for((u, s) <- l) { s.sell_to(time, buyer, commodity, u) };
//    val sold = units - left_over;
//
//    order_history.add(time, SalesRecord(buyer, List(), units, sold, p.toInt));
//    left_over
//  }
//
//  private def bestMatch(commodity: Commodity, units: Int,
//                         exclude: Owner) : (Int, List[(Int, Seller)]) = {
//    /** lowest price first + in function of the clientScore of the seller (substract clientscore% to price for selecting)
//     *for example: selling at 100 unit with client score of 5 yield to selling at 100 - 5%of100 = 95
//     * TODO might need something else afterwards than only - some % */
//    val asks = forSaleCommodities(commodity).filter((s: Seller) =>
//      (s.price(commodity) != None) && (s != exclude)).toList
//      .sorted(Ordering.by[Seller, Double](
//        (s: Seller) => {
//          s.price(commodity).get - s.contactNetwork.getContactScore(exclude.asInstanceOf[Seller])/100 * s.price(commodity).get
//        }))
//
//    greedy_match(asks, ((s: Seller) => s.available(commodity)), units)
//  }
//
//  private def compute_price(l: List[(Int, Seller)], commodity: Commodity) =
//    l.map((t: (Int, Seller)) =>
//      t._2.price(commodity).getOrElse(1.0/0) * t._1).sum
}