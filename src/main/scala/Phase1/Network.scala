package network{

  import enum.TypeLand._

  class Network(){

  }

  class Land(){ 

    var landType: TypeLand = Unknown
    val connectedTo :  Road = ???
    val owner : Any = ??? //TODO see how to implement it

  }

  class Road(){

  }

  class Intersection(){

  val intersection: List[Road] = ???

  }
}