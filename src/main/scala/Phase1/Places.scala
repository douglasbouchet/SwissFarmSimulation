package places{

  import Agents._
  import network._
  import owner._
  import enum.TypeLand._

  //This is the interface for places that are involved in economics activities 
  //(e.g land, supermarket, slaughterhouses, butcher, ...)
  //but also simple houses for population management etc... 
  trait Place{

    val connectedTo: Road
    var location: (String,String,String) //In the form Canton/district/city
    var placePossessor: Sim

  }

  class Land(_connectedTo: Road, _location: (String,String,String), _surface: Int, _placePossessor: Sim) extends Place {
    
    var landType: TypeLand = Unknown
    val connectedTo :  Road = _connectedTo
    var location : (String,String,String) = _location
    var placePossessor : Sim = _placePossessor 
    var surface : Int = _surface // in ha
    
  } 
  class Commercial(_connectedTo: Road, _location: (String,String,String), _placePossessor : Sim) extends Place {

    val connectedTo :  Road = _connectedTo
    var location = _location 
    var placePossessor : Sim = _placePossessor //TODO see how to implement it
  }

  class House(){

  }

}