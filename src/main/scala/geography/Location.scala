package geography
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
  var city: String
}