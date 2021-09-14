var contacts = scala.collection.mutable.Set[(String, Int)]()
var comToContacts = scala.collection.mutable.Map[String, scala.collection.mutable.Set[String]]()

contacts.add(("Doug",1))
contacts.add(("Shan",1))

comToContacts.update("Wheat", comToContacts.getOrElse("Wheat", scala.collection.mutable.Set[String]()).union(Set("Doug")))
comToContacts.update("Wheat", comToContacts.getOrElse("Wheat", scala.collection.mutable.Set[String]()).union(Set("Bryan")))
comToContacts.update("Barley", comToContacts.getOrElse("Barley", scala.collection.mutable.Set[String]()).union(Set("Shan")))


contacts

comToContacts

def contactsSellingCom(com: String): List[String] = {
  comToContacts.getOrElse(com, Set[String]()).toList
}

contactsSellingCom("Wheat")