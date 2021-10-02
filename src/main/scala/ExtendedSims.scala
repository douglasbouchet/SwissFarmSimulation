import Simulation.{SimO, Simulation}
import geography.City
import geography.Location

/** This type of Sim are positionable, which mean they can move, and we can take into account emission of GHG when travelling */
abstract class PositionableSim(shared: Simulation, start_time: Int = 0, startCity: City) extends SimO(shared, start_time) with Location {

}