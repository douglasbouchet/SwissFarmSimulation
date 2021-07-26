package Agents{

  import DougSimulation._
  //import Securities.Commodities._
  import cowState._
  import Goods._
  import Numbers._
  import Market._

  



  trait Agents{

    val sim : DougSimulation

    
    def init(){
    }

    def stat(){

    }
    def findSupplies(market: Market): Boolean = {
      println("findSupplies: This should have been overrided")
      false
    }
    def updateState(market: Market){
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

    case class Farm(){
    //TODO
    }
    
    //TODO
    case class Person(sim : DougSimulation) extends Agents{
      var required : List[(Goods,Int)] = ???
      var produced : List[(Goods,Int)] = ???
      val s : DougSimulation = sim
    }

    case class CattleFarm(sim : DougSimulation, bio: Boolean, nEmployee: Int, initHerd: List[Cow]) extends Agents{

      val s : DougSimulation = sim
      var required : List[(Goods,Int)] = List()
      var produced : List[(Goods,Int)] = List()
      
      var CO2 : Double = 0.0

      var employee: List[Person] = List()
      var herd : List[Cow] = initHerd

      var stateCounter : Int = 0

      override def init(){
        stat()
      }
      override def stat(){
        println("The CattleFarm has " + employee.length + " employee, and " + herd.length + " cows.")
      }

      override def findSupplies(market: Market): Boolean = {
        true
      }

      //each turn, ask for feedstuff for cows
      // produced a cow if its state is ready to be eat, but one at a time for the moment
      // inc. cows if one is pregnant since 6 turn 
      override def updateState(market: Market){
        stateCounter += 1
        produced = List()
        required = List((Wheat,herd.map(cow => cow.quantityFeedstuff).sum)) // add vaccin etc after 
        herd.foreach(cow => 
          if(cow.state == pregnant) {
            if (cow.pregnantSince >= PREGNANCY_DURATION){
              val newCow = Cow(s, false, 100)
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


    

    case class Cow(sim: DougSimulation, initOrganicFeedstuff: Boolean, initHealth: Int)
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

      override def init(){
          stat()
      }

      override def findSupplies(market: Market) : Boolean = {
        var obtained = true
        // TODO problem si plusieurs required, on peut prendre premier good mais si fail au deuxieme, le marché a quand même
        // été débité du premier, donc voir comment mieux gérer ca (avec une liste de possédé etc)
        required.foreach(item =>{
          //println("The item required is :" + item)
          if(market.getProduct(item._1,item._2) == false){
            obtained = false
          }
        })
        //println("The value of obtained is: " + obtained)
        obtained
      }

      override def updateState(market: Market){
        stateCounter += 1
        age = stateCounter / 12
        findSupplies(market) match {
          case x if x == true & weight < 700 => weight += randomBetween(6,13)
          case false => weight -= randomBetween(3,7) //discrete for the moment but weight will be udp. in f(quantity eaten)
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
      override def stat(){
        println("Cow = age: " + age +  " weight: "  + weight + " state: " + state + " quantity of food to find: " + quantityFeedstuff)
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