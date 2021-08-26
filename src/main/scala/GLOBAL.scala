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

  final val WHEAT_SEEDS_PER_HA: Double = 0.15
  final val WHEAT_PRODUCED_PER_HA: Double = 6.0
  final val HA_PER_WORKER: Int = 5 
}

