package Agents{

  import Simulation._
  //import Securities.Commodities._
  import cowState._
  import Goods._
  import scala.collection.mutable.ListBuffer


  trait Agents{

    def findSupplies(): Boolean = {
      println("findSupplies: This should have been overrided")
      false
    }
    def updtateState(){
      println("updtateState: This should have been overrided")
    }

    var consumed : ListBuffer[Map[Goods, Int]] // this evolved at each state 
    var produced : ListBuffer[Map[Goods, Int]] // this evolved at each state
  }

   trait Animals extends Agents{
    var organicFeedstuff: Boolean // (start with something simple, but then extends to all kinds of cereals etc)
    var age: Int // in year
    var health: Int //-> usefull to determine quality of the meat in f. of feedstuff, environnement, vaccin)
    var weight: Int // in kg
    var quantityFeedstuff : Int // (f(weight,age)) (maybe juste f(weight) atm) 
   }
    
    
    abstract class Supplier{
      //val goods : List[collection.mutable.Map[Commodity,Int]]
      val clients : List[Farm]
      //val suppliers : ? (assume Suppliers are acting as source atm)
      //val location : Location(type to define)
    }

    class Farm(){
      //TODO
    }
    
    //TODO
    case class Person() extends Agents{
      var consumed : ListBuffer[Map[Goods, Int]] = ???
      var produced : ListBuffer[Map[Goods, Int]] = ???
    }

    case class CattleFarm(bio: Boolean, nEmployee: Int, initHerd: List[Cows]) extends Agents{

      var consumed : ListBuffer[Map[Goods, Int]] = ListBuffer(Map((Wheat -> initHerd.map(cow => cow.quantityFeedstuff).sum)))
      var produced : ListBuffer[Map[Goods, Int]] = ListBuffer(Map())

      var employee: List[Person] = List()
      var herd : List[Cows] = initHerd

      var stateCounter : Int = 0

      override def findSupplies(): Boolean = {
      println("findSupplies: This should have been overrided")
      false
      }
      //each turn, ask for feedstuff for cows
      // produced a cow if its state is ready to be eat, but one at a time for the moment
      // inc. cows if one is pregnant since 6 turn 
      override def updtateState(){
        stateCounter += 1
        //println("QUantity required: " + herd.map(cow => cow.quantityFeedstuff).sum)
        consumed :+ Map(Wheat -> herd.map(cow => cow.quantityFeedstuff).sum)
        println("Goods needed are : ")
        consumed.foreach(event => println(event))
        println((Wheat -> herd.map(cow => cow.quantityFeedstuff).sum))

      }

      //TODO
    }


    

    case class Cows(initOrganicFeedstuff: Boolean, initHealth: Int)
      extends Animals{

      var consumed = ListBuffer(Map(Wheat -> 10)) // maybe not necessecary inside the cow class, but more on the cattlefarmer class
      var produced = ListBuffer(Map(Beef -> 10))

      var organicFeedstuff = initOrganicFeedstuff
      var health = initHealth
      var age = 0
      var weight = 40
      var quantityFeedstuff = weight/10
      var state: CowState = calf
      var stateCounter : Int = 0

      override def findSupplies() : Boolean = {
        true
      }

      override def updtateState(){
        stateCounter += 1
        age = stateCounter / 12
        findSupplies match {
          case x if x == true & weight < 700 => weight += 10
          case false => weight -= 5 //discrete for the moment but weight will be udp. in f(quantity eaten)
          case _ => 
          }
        quantityFeedstuff = weight/10

        stateCounter match {
          case x if x > 36 & state != pregnant => state = rdyToBeEat
          case _ => 
        }
        stat()
      }
      def stat(){
        println("age: " + age +  " weight: "  + weight + " state: " + state + " quantity of food to find: " + quantityFeedstuff)
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