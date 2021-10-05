package market

import Securities.Commodities.Commodity
import Simulation.{SimO, Simulation}
import code._

import scala.collection.mutable

//This class is used to represent commodities or prices, that oscillate

/**
 *
 * @param s
 * @param baseValue
 * @param _updateFrequency
 * @param _variation
 * @param _period
 * @param _fluctSize
 */
abstract class Oscillator(s: Simulation, baseValue: mutable.Map[Commodity, Int] = mutable.Map(), _updateFrequency: Int = 1, _variation: Float = 0.2f, _period: Int = 2, _fluctSize: Float = 0.05f) extends SimO(s){

  val variation: Float = _variation //The amplitude of the sinusoid
  val period: Int = _period //The period of the sinusoid (in year)
  val fluctSize: Float = _fluctSize //Small jumps based on base price, to not have a perfect sinusoid
  val updateFrequency: Int = _updateFrequency

  private val timeStep: Double = 2*Math.PI / (12 * period * CONSTANTS.TICKS_TIMER_PER_DAY) // For a period of 2 years, if each timestep is 1 month
  private var counter: Int = 0
  private val rnd = scala.util.Random

  //The base [quantity/price] for each commodity. in [tones/chf]
  var baseComValue: scala.collection.mutable.Map[Commodity, Double] = mutable.Map[Commodity, Double]()//constant
  var trueComValue: scala.collection.mutable.Map[Commodity, Double] = mutable.Map[Commodity, Double]() //fluctuate

  def updateValue(com: Commodity): Unit = {
    val oldComValue : Double = baseComValue(com)
    trueComValue.update(com,
      BigDecimal(oldComValue*(1 + variation*Math.sin(timeStep * counter)) - fluctSize * oldComValue + rnd.nextInt((2 * fluctSize * oldComValue).toInt)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
  }

  def updateAllValues(): Unit = {
    trueComValue.keySet.foreach(updateValue)
    counter += 1
  }

  def getValueOf(com: Commodity): Double = {
    trueComValue(com)
  }

  def stats(): Unit = {
    print("Value of commodities: ")
    trueComValue.keySet.foreach(com => {
      print(com + "=" + getValueOf(com))
    })
    println()
  }

  /** Update the value each (updateFrequency) day */
  override protected def algo: Instruction = __forever(
    __if(s.timer % updateFrequency*CONSTANTS.TICKS_TIMER_PER_DAY == 0){
      __do{
        updateAllValues
      }
    },
    __if(s.timer % 365*CONSTANTS.TICKS_TIMER_PER_DAY == 0){
      __do{
        //stats()
      }
    },
    __wait(1)
  )

  override def mycopy(_shared: Simulation, _substitution: mutable.Map[SimO, SimO]): SimO = ???
}
