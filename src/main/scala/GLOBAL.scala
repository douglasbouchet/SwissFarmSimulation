package GLOBAL {

class Dummy;

}

package object GLOBAL {
  var silent = false 
  val rnd = util.Random

  def mapopt[A,B](l: List[A], f: A => Option[B]) : List[B] =
    l.flatMap((a: A) => f(a) match {
      case Some(b) => List(b)
      case None    => List()
    })
}

package object CONSTANTS {

  val WHEAT_SEEDS_PER_HA: Double = 0.15
  val WHEAT_PRODUCED_PER_HA: Double = 6.0
  val HA_PER_WORKER: Int = 5 
  val CONVERSION_WHEAT_FLOUR: Double = 0.8
  var workercounter: Int = 0
  val KG_CO2_PER_WHEAT_CROP_HA: Double = 1900
}

