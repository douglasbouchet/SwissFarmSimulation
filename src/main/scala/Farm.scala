package farmpackage{

  import Simulation._
  import Simulation.Factory._
  import landAdministrator.CadastralParcel
  import landAdministrator.LandOverlay
  import landAdministrator.LandOverlayPurpose._
  import code._
  import Securities.Commodities._

import scala.collection.mutable

  /** extends seller, owner */ 
  //case class Farm(s: Simulation) extends SimO(s,0) with MultipleActionsSim {
  //case class Farm(s: Simulation, prod: ProductionLineSpec) extends Factory(prod,s) {
    /**extends Factory(new ProductionLineSpec(1,List(),List(),(Wheat,0), 0),s) */
  case class Farm(s: Simulation) extends SimO(s){

    var parcels: List[CadastralParcel] = List()
    var landOverlays: List[LandOverlay] = List()
    var name = "farm"
    var rpt: Int = 0
    var crops: List[Factory] = List()
    //val wheatCrop1 = new Factory(new ProductionLineSpec(1, List(),List((WheatSeeds,10)), (Wheat,10),6), s, this)
    //val wheatCrop2 = new Factory(new ProductionLineSpec(1, List(),List((WheatSeeds,20)), (Wheat,20),6), s, this)
    //crops ::= wheatCrop1
    //crops ::= wheatCrop2


    def addParcels(newParcels: List[CadastralParcel]) {
      parcels :::= newParcels
    }

    def actions: List[(Instruction, Int)] = ???

    override def stat = {
      //println(s"$name \n " + inventory_to_string() + " end")
      println(s"$name \n " + landOverlays+  " end")
    }
    override def algo = __forever(__wait(1))
    // override def algo = __forever(
    //  __dowhile(
    //    __wait(1),
    //    __do{
    //      println("Je dis bonjour ");
    //      rpt += 1;
    //      println(s"rpt = $rpt")
    //    }
    //  )({ rpt < 6}),
    //  __wait(1),
    //  __do(this.rpt = 0),
    //  __do(println("Starting over"))
    // )

    override def mycopy(_shared: Simulation, _substitution: mutable.Map[SimO,SimO]): SimO = ???

    /**Create a factory for each landOverlay of purpose the farm has
     * ProductionLineSpec is determined in function of area, and purpose of LandOverlay
     */
    def init = {
      landOverlays.foreach(lOver => {
        if(lOver.purpose == wheatField){
          //after we can add more complex attributs for productivity
          val area: Double = lOver.getSurface
          val prodSpec = new ProductionLineSpec(math.round((area/3).toFloat), List((WheatSeeds, math.round((area/10).toFloat))), List(), (WheatSeeds, math.round((area*10).toFloat)), 6)
          crops ::= new Factory(prodSpec,s, this)
        }
      })
      s.sims :::= crops
    }
  }

  
}