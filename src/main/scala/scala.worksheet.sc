import Owner._

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

val net = new ContactNetwork

val seller0 = new Seller
val seller1 = new Seller
val seller2 = new Seller
val sellerNotPresent = new Seller

net.addContact(seller0)
net.addContact(seller1)
net.addContact(seller2)

net.removeContact(seller0)
net.getContactScore(seller0)
net.getContactScore(seller1)
net.contacts

net.decreaseScore(seller1)


net.contacts