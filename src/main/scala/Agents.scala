package Agents{

  import Simulation._
  import Securities.Commodities._
  import code._
  import cowState._

  trait Agents{

    def findSupplies(){
      println("findSupplies: This should have been overrided")
    }
    def updtateState(){
      println("updtateState: This should have been overrided")
    }
  }

  //abstract class Animals(shared: Simulation,
  //abstract class Animals(
  //  val organicFeedstuff: Boolean, // (start with something simple, but then extends to all kinds of cereals etc)
  //  //val Owner: Farm
  //  val health: Int //-> usefull to determine quality of the meat in f. of feedstuff, environnement, vaccin)
  // )extends Agents{
  //  var age: Int // in year
  //  var weight: Int // in kg
  //  var quantityFeedstuff : Int // (f(weight,age)) (maybe juste f(weight) atm) 
  // }

   trait Animals extends Agents{
    var organicFeedstuff: Boolean // (start with something simple, but then extends to all kinds of cereals etc)
    var age: Int // in year
    var health: Int //-> usefull to determine quality of the meat in f. of feedstuff, environnement, vaccin)
    var weight: Int // in kg
    var quantityFeedstuff : Int // (f(weight,age)) (maybe juste f(weight) atm) 
   }
    
    
    abstract class Supplier{
      val goods : List[collection.mutable.Map[Commodity,Int]]
      val clients : List[Farm]
      //val suppliers : ? (assume Suppliers are acting as source atm)
      //val location : Location(type to define)
    }



    class Farm(){
      //TODO
    }

    case class Cows(initOrganicFeedstuff: Boolean, initHealth: Int)
      extends Animals{

      var organicFeedstuff = initOrganicFeedstuff
      var health = initHealth
      var age = 0
      var weight = 40
      var quantityFeedstuff = weight/10
      var state: CowState = calf
      var stateCounter : Int = 0





      def stat(){
        println("age: " + age +  " weight: "  + weight + " state: " + state)
      }

      // ----------------- NOT SURE
      //override def mycopy(_shared: Simulation.Simulation,_substitution: scala.collection.mutable.Map[Simulation.SimO,Simulation.SimO]): Simulation.SimO = {
      //  val n = new Cows(s,age,weight,organicFeedstuff,quantityFeedstuff,health);
      //  copy_state_to(n);
      //  n
      //}
      //def action: code.Instruction = __do{}
      // ----------------------------
    }


}