package Simulation
import SimLib._
import Securities.Commodities._
import farmpackage.Farm
import Simulation.Factory._
import Markets._
import Simulation.Simulation
import generation.Generator
import geography.LandAdministrator
import glob._

object MainSwissFarmSimulation {
  val s = new Simulation

  val silo         = new Trader(Wheat, 100, s);
  //val flour_trader  = new Trader(Flour, 50, s);
  // val flour_buyer     = new Buyer(Flour, () => 40, s);

  val canton = "Glaris"
  val generator = new Generator(canton)
  val landAdministrator = new LandAdministrator(s, canton)

  generator.generateAgents(landAdministrator, s)

  //def main(argv: Array[String]) {
  //  if((argv.length != 1) || (argv(0).toInt < 1))
  //    println("Exactly one integer >0 argument needed!");
  //  else
  //    s.run(argv(0).toInt);
  //}
  def main(argv: Array[String]) {
    s.run(500);
  }
}

//ADD MOULIN DANS SIMULATION ET VOIR SI CA MARCHE



object TradingExample {
  import Owner._;

  val simu = new Simulation;

  val s = new Source(Wheat, 4, 1000*100, simu);
  val t = new Trader(Wheat, 1,           simu);
  val b = new  Buyer(Wheat, () => 1,     simu);

  simu.init(List(s, t, b));
  simu.run(4);
/* After 4 steps we have
(BalanceSheet(4000,4000,4000,0,0),ArrayBuffer(Wheat -> 0@0))
(BalanceSheet(50,50,50,0,0),ArrayBuffer(Wheat -> 0@1000))
(BalanceSheet(0,4050,0,-4050,0),ArrayBuffer(Wheat -> 4@1012))
*/
}



