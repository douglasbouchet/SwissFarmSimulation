package geography

import Simulation.SimO

/**
 * This package will contains all classes/trait related to geography i.e The roads, and Location for the moment
 */


/** Should be inherited by every agents/Land/LandOverlay that should be positionable
 * Milestone 1: Only contains {country = Switzerland, Canton, District and City}. Road network Nodes should not be
 * more precise than cities
 * Milestone 2: Add full addresses (different locations inside a city) + maybe an absolute value like GPS coordinates
 * Distances between cities are known inside the road network
 * @note name are string for the moment, but they should be values inside a Database or whatever.
 */
trait Location {
  var country: String = "Switzerland"
  var canton: String
  var district: String
  var city: City
  //TODO replace by City ?

  //donner accès au LocationAdministrator's method -> keep extend Location (main purpose)

  /**
   * @param radius in meters
   * @return a list of SimO at a distance less or equal to radius from the current city
   */
  def findNearSimO(radius: Int): List[SimO] = {
    LocationAdministrator.findSimOInRadius(city, radius)
  }

  def moveToCity(newCity: City): Unit = {
    city = newCity
  }
}



object LocationAdministrator {

  /** For operations like finding agents near a given location, we could store Cities inside District and check only cities inside the canton of the given location
   * Simplify the runtime, but exclude some farms, which may not be the case in real life. So store all cities inside a list, w/o differentiations */
  var cities: List[City] = List[City]()

  def init(): Unit = {

  }

  //From https://www.movable-type.co.uk/scripts/latlong.html
  /** compute shortest distance between 2 cities (in meter)
   * Not perfect because in reality distance depends on roads, and this function skip this detail, but use this while we don't have any road data */
  private def computeDistanceBetweenCities(cityA: City, cityB: City): Double = {
      val earthRadius: Int = 6371000 //in meter
      val phi1: Double = cityA.centerCoord._1 * Math.PI/180
      val phi2: Double = cityB.centerCoord._1 * Math.PI/180
      val deltaPhi: Double = (cityB.centerCoord._1 - cityA.centerCoord._1) * Math.PI/180
      val deltaLambda: Double = (cityB.centerCoord._2 - cityA.centerCoord._2) * Math.PI/180
      val a: Double = Math.pow(Math.sin(deltaPhi/2),2) + Math.cos(phi1) * Math.cos(phi2) * Math.pow(Math.sin(deltaLambda), 2)
      earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  }

  /**
   * @param radius in meters
   * @return a list of SimO at a distance less or equal to radius from city "from"
   * @note afterwards, this could use the road network, once it has real roads
   */
  def findSimOInRadius(from: City, radius: Int): List[SimO] = {
    val nearCities: List[City] = cities.filter(city => computeDistanceBetweenCities(city, from) <= radius)
    nearCities.foldLeft(List[SimO]()){
      (acc: List[SimO], num: City) => acc ::: num.agents
    }
  }


}


/** This can be used as a first milestone to represent the nodes of the RoadNetwork (not really precise, but should do the job)
 * @param name of the city
 * @param district city is part of
 * @param canton city is part of
 * @param center gps coordinates in the form: (Latitude, Longitude) in degree
 */
class City(_name: String, _district: String, _canton: String, _centerCoord: (Double, Double)) {
  val name: String = _name
  val district: String = _district
  val canton: String = _canton
  val centerCoord: (Double, Double) = _centerCoord

  //The agents currently inside the city (can be farms, markets, ...)
  //Maybe in next version consider a Map[SimO instance (e.g Farm, Mill,...) -> List of those]
  var agents: List[SimO] = List[SimO]()

  def addAgent(agent: SimO): Unit = {
    if(!agents.contains(agent)) agents ::= agent
    else println("Trying to add an agent already present in the city")
  }

  def remAgent(agent: SimO): Unit = {
    if(agents.contains(agent)) agents = agents.filterNot(_ == agent)
    else println("Trying to remove an agent not present in the city")
  }
}