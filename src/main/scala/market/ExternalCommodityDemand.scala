package market

import Securities.Commodities.{CanolaOil, Commodity, FeedStuff, Fertilizer, Flour, Wheat, WheatSeeds}
import Simulation.{SimO, Simulation}
import glob.Observator

import scala.collection.mutable

/**
 * Agent representing the demand from external countries
 * Milestone 1:
 * - Aggregate demands coming from differents countries inside one global demand
 * - The demand is a sinusoid + depends on ((past year) or (current)???) domestic prices (past year price atm)
 */
class ExternalCommodityDemand(s: Simulation, obs: Observator) extends Oscillator(s) {

  baseComValue = scala.collection.mutable.Map[Commodity, Double](
    Wheat -> 2000,
    Flour -> 500,
    CanolaOil -> 300
  )
  trueComValue = scala.collection.mutable.Map[Commodity, Double](
    Wheat -> 0,
    Flour -> 0,
    CanolaOil -> 0
  )

}
