package Agents{

  import DougSimulation._
  //import Securities.Commodities._
  import cowState._
  import Goods._
  import Constants._
  import Market._


  trait Agents{

    val sim : DougSimulation

    
    def init(){

    }
    def findSupplies(market: Market): Boolean = {
      println("findSupplies: This should have been overrided")
      false
    }
    def updtateState(){
      println("updtateState: This should have been overrided")
    }

    var required : List[(Goods, Int)] // this evolved at each state 
    var produced : List[(Goods,Int)] // this evolved at each state
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
    case class Person(sim : DougSimulation) extends Agents{
      var required : List[(Goods,Int)] = ???
      var produced : List[(Goods,Int)] = ???
      val s : DougSimulation = sim
    }

    case class CattleFarm(sim : DougSimulation, bio: Boolean, nEmployee: Int) extends Agents{

      val s : DougSimulation = sim
      var required : List[(Goods,Int)] = List()
      var produced : List[(Goods,Int)] = List()

      var employee: List[Person] = List()
      var herd : List[Cow] = List()

      var stateCounter : Int = 0

      override def init(initherd : List[Cow]){
        herd = initherd
      }

      override def findSupplies(market: Market): Boolean = {
        println("findSupplies: This should have been overrided")
        false
      }

      //each turn, ask for feedstuff for cows
      // produced a cow if its state is ready to be eat, but one at a time for the moment
      // inc. cows if one is pregnant since 6 turn 
      override def updtateState(){
        stateCounter += 1
        produced = List()
        required = List((Wheat,herd.map(cow => cow.quantityFeedstuff).sum)) // add vaccin etc after 
        herd.foreach(cow => 
          if(cow.state == pregnant) {
            if (cow.pregnantSince >= PREGNANCY_DURATION){
              val newCow = Cow(s, false, 100, this)
              println("New Cow")
              herd = herd :+ newCow
              sim.addAgent(newCow)
            }
          })
        if(stateCounter % 12 == 0){
          herd.foreach(inseminateCow(_))
        }

        if(herd.length > 5){
          killCow()
        }

      }

      def inseminateCow(_cow: Cow){
        if(_cow.age >= 3 & _cow.state != `pregnant`){
          _cow.state = pregnant
        }
      }

      def killCow(){
          val killed : Cow = herd.head
          herd = herd.tail
          s.remAgent(killed)
          produced = List((Beef, killed.weight))
          println("Killing a cow provided: " + produced)
      }

      
    }


    

    case class Cow(sim: DougSimulation, initOrganicFeedstuff: Boolean, initHealth: Int, owner: CattleFarm)
      extends Animals{
      
      val s : DougSimulation = sim
      var required = List((FeedStuff,10)) // maybe not necessecary inside the cow class, but more on the cattlefarmer class
      var produced = List((Beef,0))

      var organicFeedstuff = initOrganicFeedstuff
      var health = initHealth
      var age = 0
      var weight = 40
      var quantityFeedstuff = weight/10
      var pregnantSince : Int = 0
      var state: CowState = calf
      var stateCounter : Int = 0

      override def findSupplies(market: Market) : Boolean = {
        true
      }

      override def updtateState(market: Market){
        stateCounter += 1
        age = stateCounter / 12
        findSupplies(market) match {
          case x if x == true & weight < 700 => weight += 10
          case false => weight -= 5 //discrete for the moment but weight will be udp. in f(quantity eaten)
          case _ => 
          }
        quantityFeedstuff = weight/10

        stateCounter match {
          case x if x > 36 & state != pregnant => state = rdyToBeEat
          case _ => 
        }
        state match{
          case `pregnant` => {
            if (pregnantSince < PREGNANCY_DURATION){
              pregnantSince += 1
            }
            else {
              state = rdyToBeEat
              pregnantSince = 0
            }
          }
          case _ =>
        }
        
        //stat()
      }
      def stat(){
        println("age: " + age +  " weight: "  + weight + " state: " + state + " quantity of food to find: " + quantityFeedstuff + 
        " pregnant since : " + pregnantSince)
      }

       override def equals(that: Any): Boolean = {
         that match {
           case that: Cow => this eq that
           case _ => false
         }
      }

      // ----------------- NOT SURE
      //override def mycopy(_shared: Simulation.Simulation,_substitution: scala.collection.mutable.Map[Simulation.SimO,Simulation.SimO]): Simulation.SimO = {
      //  val n = new Cow(s,age,weight,organicFeedstuff,quantityFeedstuff,health);
      //  copy_state_to(n);
      //  n
      //}
      //def action: code.Instruction = __do{}
      // ----------------------------
    }


}