package network

  import places._
  
  class Road(_name: String, _length: Int){
    
    val name: String = _name
    val length: Int = _length
    //val between : (Intersection, Intersection) = (i1, i2) not necessary, when starting at a land, we could just 
    // see which other roads are connected to the one the land is connected to
    //add after some properties on the road(capactity, privacy etc...)
  }
      
  class Intersection(){
        
    val intersection: List[Road] = ???
        
  }

  class RoadSystem(){

    val edges : List[Road] = List()
    val nodes : List[Intersection] = List()

    //computes all the roads, intersection and land, based on some data
    def init() = ???

    def findShortedPath(place1 : Place, place2 : Place) = ???

  }