package Simulation
import SimLib._
import Securities.Commodities._


object MainExample {
  val s = new Simulation;

  val f1     = new Farm(s, 20);
  val f2     = new Farm(s, 40);
  val m      = new Mill(s, 30);
  val bakery = new Bakery(s, 40)
  val cattleFarm  = new CattleFarm(s, 2);
  val butcher = new Butcher(s,1)
  //val c   = new Cinema(s);
  //val mcd = new McDonalds(s);
  val landlord        = new Source(Land,  20, 100000*100, s);
  val seedsProvider   = new Source(WheatSeeds, 1000, 1*100, s)
  val feedStuffProvider   = new Source(FeedStuff, 1000, 1*100, s)
  //val pregnantCow     = new Source(Cows, 1000, 6000*100, s)
  //val cattle_farmer = new Source(Beef,   100,  26000*100, s);
  //val silo          = new Source(Wheat, 1000,   6668*100, s);
  //val silo2         = new Trader(Wheat, 100, s)
  //val flour_trader  = new Trader(Flour, 50, s);
  //val flour_buyer   = new Buyer(Wheat, () => 20, s);

  val people = for(x <- 1 to 12) yield new Person(s, true);

  s.init(List(
    landlord,
    seedsProvider,
    feedStuffProvider,
    wheatTrader,
    //pregnantCow,
    //silo,
    // silo2, 
    //flour_trader,
    cattleFarm,
    butcher,
    f1,f2, m, bakery
    // c, rf, mcd,
    //flour_buyer
  ) ++ people.toList);


  def main(argv: Array[String]) {
    println("The arguments are : " + argv.head);
    println(argv.tail.head)
    //if((argv.length != 1) || (argv(0).toInt < 1))
    //  println("Exactly one integer >0 argument needed!");
    //else
    println(s.market)
    for (i <- 1 to 6){
      //println("The number of cows is: " + cattleFarm.nCows)
      //var old_pl =  cattleFarm.pl
      //var old_spec = cattleFarm.pl.head
      //cattleFarm.pl = List(new ProductionLine(new ProductionLineSpec(
      //  old_spec.employees_needed,
      //  old_spec.required
      //)))
      s.run(1);
    }
      //s.run(argv(0).toInt);
  }
}

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



