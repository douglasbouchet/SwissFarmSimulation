package Government

import CONSTANTS.PROD_MAP
import Securities.Commodities.Commodity
import geography.LandOverlay

import scala.collection.mutable

sealed trait Policy {
  val name: String = "not implemented policy"

  //def projectIncomes(lovers: List[LandOverlay], budget: Int): (List[LandOverlay], Int)
  def projectIncomes(lovers: List[LandOverlay], budget: Int): Unit

  /** Compute a map which map each commodity to its quantity produced, for all landOverlays */
  def landOverlaysToUnit(lOvers: List[LandOverlay]): mutable.Map[Commodity, Int] = {
    val comToUnit = scala.collection.mutable.Map[Commodity, Int]()
    lOvers.foreach(lOver => {
      val (com, quantity) : (Commodity, Int) = PROD_MAP(lOver.prevPurpose)._2.map(tup => (tup._1, (tup._2 * lOver.getSurface).toInt)).head
      comToUnit.put(com, comToUnit.getOrElse(com, 0) + quantity)
    })
    comToUnit
  }
}

//TODO do we pass lOvers with already some purpose ? should be yes
//Also, do we want to get the max incomes for the given purpose distribution for the land Overlays,
//or we want to propose new landOverlays that could maximize the imports ?

case class ConvertToOrganic() extends Policy {
  override val name = "ConvertToOrganic"
  //override def projectIncomes(lOvers: List[LandOverlay], budget: Int): (List[LandOverlay], Int) =
  override def projectIncomes(lOvers: List[LandOverlay], budget: Int): Unit =
    println("The production should be " + landOverlaysToUnit(lOvers))
}
