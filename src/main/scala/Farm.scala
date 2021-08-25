package farmpackage{

  import Simulation._
  import Simulation.Factory._
  import landAdministrator.CadastralParcel
  import code._
  import Securities.Commodities._

import scala.collection.mutable

  /** extends seller, owner */ 
  //case class Farm(s: Simulation) extends SimO(s,0) with MultipleActionsSim {
  //case class Farm(s: Simulation, prod: ProductionLineSpec) extends Factory(prod,s) {
    /**extends Factory(new ProductionLineSpec(1,List(),List(),(Wheat,0), 0),s) */
  case class Farm(s: Simulation) extends SimO(s){

    var parcels: List[CadastralParcel] = List()
    var name = "ferme de douglas"
    var rpt: Int = 0
    val bonjour = new Bonjour(s)
    val wheatFactory = new Factory(new ProductionLineSpec(1, List(),List(), (Wheat,10),3), s, this)


    def addParcels(newParcels: List[CadastralParcel]) {
      parcels :::= newParcels
    }

    def actions: List[(Instruction, Int)] = ???

    override def stat = {
      println(s"$name " + wheatFactory.inventory_to_string())
    }
    //override def algo = __forever(__do(println("Je dis bonjour ")), __wait(2))
    override def algo = __forever(
      __dowhile(
        __wait(1),
        __do{
          println("Je dis bonjour ");
          rpt += 1;
          println(s"rpt = $rpt")
        }
      )({ rpt < 6}),
      __wait(1),
      __do(this.rpt = 0),
      __do(println("Starting over"))

    )

    // override def run_until(until: Int) : Option[Int] = {
    //   println("Coucou")
    //   // val nxt1 = super.run_until(until).get;
    //   // val nxt2 = pl.map(_.run_until(until).get).min;
    //   //Some(math.min(nxt1, nxt2)) // compute a meaningful next time
    //   Some(until)
    // }

    override def mycopy(_shared: Simulation, _substitution: mutable.Map[SimO,SimO]): SimO = ???
  }

  class Bonjour(s: Simulation) extends SimO(s) {
    
    def x = println("lol")

    def actions: List[(Instruction, Int)] = ???

    override def algo = __forever(__do(println("Je suis bonjour Je dis bonjour ")), __wait(1))

    override def mycopy(_shared: Simulation, _substitution: mutable.Map[SimO,SimO]): SimO = ???
  }


//
//  override def algo = 
//    __do{
//        println("starting the wheat production")
//      __dowhile(
//        __wait(1),
//        __do(rpt += 1)
//        )(rpt < 5)
//        make(Wheat, 20, 1)
//        println("wheat production ended")
//    }
//        
//  override def mycopy(_shared: Simulation, _substitution: mutable.Map[SimO,SimO]): SimO = ???
}