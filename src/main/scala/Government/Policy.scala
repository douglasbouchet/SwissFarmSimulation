package Government

import CONSTANTS.PROD_MAP
import Securities.Commodities.Commodity
import geography.LandOverlay

import scala.collection.mutable

sealed trait Policy {
  val name: String = "not implemented policy"

  def projectIncomes(lovers: List[LandOverlay], budget: Int): (List[LandOverlay], Int)
  //def projectIncomes(lovers: List[LandOverlay], budget: Int): Unit

  /** Compute a map which map each commodity to its quantity produced, for all landOverlays */
  def landOverlaysToUnit(lOvers: List[LandOverlay]): mutable.Map[Commodity, Int] = {
    val comToUnit = scala.collection.mutable.Map[Commodity, Int]()
    lOvers.foreach(lOver => {
      val t = PROD_MAP(lOver.prevPurpose)._2.map(tup => (tup._1, (tup._2 * lOver.getSurface).toInt))
      if(t.nonEmpty){
        val (com, quantity) : (Commodity, Int) = t.head
        comToUnit.put(com, comToUnit.getOrElse(com, 0) + quantity)
      }
    })
    comToUnit
  }
}

//TODO do we pass lOvers with already some purpose ? should be yes
//Also, do we want to get the max incomes for the given purpose distribution for the land Overlays,
//or we want to propose new landOverlays that could maximize the imports ?
//In both case we compute the optimal imports, but if we allow to change the use of landOverlay to max incomes per policy
//Then we could achieve some global maximum instead of local maximum

case class MaximumPollution() extends Policy {
  override val name = "MaximumPollution"
  override def projectIncomes(lOvers: List[LandOverlay], budget: Int): (List[LandOverlay], Int) = {
  //override def projectIncomes(lOvers: List[LandOverlay], budget: Int): Unit =
    //println("The production should be " + landOverlaysToUnit(lOvers))
    (List(), 100)
  }
}

case class TaxPollutingCrops() extends Policy {
  override val name = "TaxPollutingCrops"
  override def projectIncomes(lOvers: List[LandOverlay], budget: Int): (List[LandOverlay], Int) = {
    //override def projectIncomes(lOvers: List[LandOverlay], budget: Int): Unit =
    //println("The production should be " + landOverlaysToUnit(lOvers))
    (List(), 50)
  }
}
