package contact

import Owner.Seller

/**
 * The Goal of this class is to reflect the fact that people have usual suppliers in real life.
 * When a Buyer wants some commodities, it checks the possible seller on the SellerMarket
 * It chose the best seller not only based on price of products, but also on the clientScore
 * (You may want to buy at higher price to your friends, but not to much so you still look at other prices)
 * It basically just contains a list of Seller, and a clientScore + some method to update this list
 */
class ContactNetwork {

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

  def increaseScore(contact: Seller): Unit = {
    val oldScore = getContactScore(contact)
    contacts -= ((contact,oldScore))
    contacts += ((contact, Math.min(oldScore + 1,10)))
  }

  def decreaseScore(contact: Seller): Unit = {
    val oldScore = getContactScore(contact)
    contacts -= ((contact,oldScore))
    contacts += ((contact, Math.max(oldScore - 1,0)))
  }
}
