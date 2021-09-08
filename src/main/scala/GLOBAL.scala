package GLOBAL {

class Dummy;

}

package object GLOBAL {
  var silent = true 
  val rnd = util.Random

  def mapopt[A,B](l: List[A], f: A => Option[B]) : List[B] =
    l.flatMap((a: A) => f(a) match {
      case Some(b) => List(b)
      case None    => List()
    })
}

package object CONSTANTS {

  val WHEAT_SEEDS_PER_HA: Double = 150 // in Kg per ha
  val WHEAT_PRODUCED_PER_HA: Double = 6000 // in Kg per ha
  val HA_PER_WORKER: Int = 5 
  val CONVERSION_WHEAT_FLOUR: Double = 0.8
  var workercounter: Int = 0
  val KG_CO2_PER_WHEAT_CROP_HA: Double = 1900
}

// package price_fluctuation {

//   class SourcePriceFluctuation(shared: Simulation) {

//     val sourceFluctDistrib = distributions.Gaussian(0,15)
//     var sourceFluct: Double = fluct.sample()
//     var nextTurn: Int = shared.timer + 1

//     def getFluctuation: Double = {
//       if (shared.timer == nextTurn){
//         sourceFluct = sourceFluctDistrib.sample()
//         nextTurn += 1
//       } 
//       sourceFluct
//     }
//   }
// }


