package farmrelated

import Securities.Commodities.{Beef, Commodity, Grass}
import Simulation.Factory.{ProductionLine, ProductionLineSpec}
import Simulation.Simulation
import code._
import farmpackage.Farm
import landAdministrator.{LandOverlay, LandOverlayPurpose, Paddock}
import landAdministrator.LandOverlayPurpose.paddock

/**
 *
 * @param owner
 * @param _landOverlay
 * @param nCows
 * @param salary
 */
class Herd(owner: Farm, _paddock: Paddock, nCows: Int, salary: Int /**AnimalType */){

  println("Creating an herd")
  assert(_paddock.purpose == LandOverlayPurpose.paddock)
  //This should change if there is no more grass on this paddock
  var paddock: Paddock = _paddock
  var cows: List[MeatCow] = List[MeatCow]()

  /**
   * each MeatCow is a productionLine and needs a productionLineSpec.
   * Clearly an employee can handle more than 1 cow, that's why we are gonna set 1 employee for the first animal of the herd,
   * and 0 employee for the other ones
   * For the moment, let's assume 1 employee per herd.
   * number of cows is not limited by paddock, but will influence speed of grass consumption,
   * and cows will need to move earlier to another paddock
   */
  def initHerd(): Unit = {
    assert(nCows > 1)
    val required: List[(Commodity, Int)] = List[(Commodity, Int)]() //TODO
    //replace feedstuff by Grass
    val consumed: List[(Commodity, Int)] = List[(Commodity, Int)]((Grass, CONSTANTS.KG_OF_GRASS_PER_COW_DAY))
    val produced: (Commodity, Int) = (Beef, CONSTANTS.KG_OF_BEEF_PER_MEATCOW)
    val timeToComplete: Int = CONSTANTS.MEATCOW_PROD_DURATION
    val boosters: Option[List[(Commodity, Int, Double)]] = None //TODO add some afters ?
    var lineSpec: ProductionLineSpec =
      new ProductionLineSpec(1, required, consumed, produced, timeToComplete, boosters)
    cows ::= new MeatCow(owner, this, paddock, owner.s, lineSpec, salary, owner.s.timer)
    //we already got our employee, next animals does not need anymore employee
    //TODO see if this does not change the first line spec
    lineSpec = new ProductionLineSpec(0, required, consumed, produced, timeToComplete, boosters)

    cows :::= (for (n <- 2 to nCows) yield new MeatCow(owner, this, paddock,owner.s, lineSpec, 0, owner.s.timer)).toList
  }

  /** This should be called each time there is no more grass on current landOverlay
   * Move the herd to the paddock with the most grass quantity. If no other paddock is available, stay on this one.
   */
  def changeOfPaddock(): Unit = {
    val candidates: List[Paddock] = owner.landOverlays.filter(landOverlay => landOverlay.isInstanceOf[Paddock] && landOverlay != cows.head.paddock)
      .map(elem => elem.asInstanceOf[Paddock])
    println(s"The candidates for migrating the cows are : $candidates")
    val nextPaddockCandidates = candidates.sortWith(_.grassQuantity > _.grassQuantity)
    nextPaddockCandidates.headOption match {
      case Some(paddock) => cows.foreach(_.paddock = paddock)
      case None => println("No paddock is available for changing, staying on the same paddock")
    }
  }


  /** Nothing to add for the moment
   * In next milestones, add fact that MeatCow is killed after production complete (for now we just assume the number of cows constant
   * so we don't kill MeatCow after prod completed)
   * @param s Simulation instance where this agent will belong
   * @param pls The productionLineSpec of this productionLine
   * @param salary TODO understamnd
   */
  class MeatCow(owner: Farm,
                herd: Herd, //Used to buy for the all herd and not a single cow (avoid multiple buy in a simple way)
                _paddock: Paddock,
                s: Simulation,
                pls: ProductionLineSpec,
                salary: Int,
                start_time: Int,
                private var rpt : Int = 0,
                private var frac : Double = 1.0,
                private var costs_consumables : Double = 0.0
               ) extends ProductionLine(pls, owner, salary, start_time) {
  //include the fact that can eat the grass contains on the paddock
    //include the fact that methane + ammonia is produced everyturn

    var dailyGrassCons: Int = 0
    if (pls.consumed.filter(_._1 == Grass).nonEmpty){
      dailyGrassCons = (pls.consumed.filter(_._1 == Grass).head._2 / CONSTANTS.TICKS_TIMER_PER_DAY)
    }
    else {
      println("There is a problem: this animal seems carnivore")
    }
    var methane: Double = 0.0
    var ammonia: Double = 0.0
    var paddock: Paddock = _paddock


    /** override because more realistic to consume grass each day/month rather than once in all the production process
     * assume that grass consumed is updated everyday (to measure how much remain on the paddock)
     * And if eating grass bought on market, command grass for 2 months (to avoid buying every turn)
     */
    override protected def algo = __forever(
      __do { // start of production run
        costs_consumables = 0
        rpt = 0
        frac = 1.0;
      },
      __dowhile(
        __do{
          //Eat grass on landOverlay if it contains some, else eat from owner inventory.
          //If missing on land, order a change of paddock, and buy some from owner to put in inventory
          if(paddock.grassQuantity > dailyGrassCons){
            paddock.grassQuantity -= dailyGrassCons
          }
          //Not enough grass, check inside inventory
          else{
            paddock.grassQuantity = 0.0 //Prendre en compte la nourriture manger ce coup ici
            val invGrass: Int = owner.available(Grass)
            //In that case, buy should buy for the all herds for 2 month (atm). + reduce the frac as cow are eating less
            if(invGrass < dailyGrassCons){
              owner.cooperative match {
                case Some(coop) => {
                  println("Buying some grass on market")
                  owner.buyMissingFromCoop(List((Grass, herd.cows.length * dailyGrassCons * 60 / CONSTANTS.TICKS_TIMER_PER_DAY)))
                  println("Now the situation is : ")
                }
                case None => owner.bulk_buy_missing(List((Grass, dailyGrassCons * 60 / CONSTANTS.TICKS_TIMER_PER_DAY)), herd.cows.length)
              }
              //avoid quite long call to destroy
              if(invGrass != 0){
                costs_consumables += owner.destroy(Grass, invGrass)
              }
              //assume that loss 0.1 % of frac each time does not eat (of course could be a function of missing and not always 0.001 but simple atm)
              frac -= 0.001
            }
            else{
              costs_consumables += owner.destroy(Grass, dailyGrassCons)
            }

            //Now that this paddock does not contains anymore grass, herd should move to another paddock
            herd.changeOfPaddock()
          }
          //ammonia & methane emissions. In the future, compute them as a function depending on diet, temperature,...
          methane += CONSTANTS.KG_METHANE_COW_DAY
          ammonia += CONSTANTS.KG_AMMONIA_COW_DAY

          rpt += 1

          //Each month (assume 30 days), pay the salary (check salary != 0 cause only one MeatCow pls has an employee)
          if(rpt%30 == 0 && salary != 0){
            goodwill += pls.employees_needed * salary;
          }
        },
        __wait(1),
      )({ rpt < pls.time_to_complete }),
      __do{
        //print("production complete! ");
        val units_produced = (pls.produced._2  * frac).toInt;
        val personnel_costs = pls.employees_needed * salary * pls.time_to_complete;
        val total_cost : Double = costs_consumables + personnel_costs;
        val unit_cost = total_cost / units_produced;

        if(units_produced > 0) {
          o.make(pls.produced._1, units_produced, unit_cost);

          if(! GLOBAL.silent)
            println(o + " produces " + units_produced + "x " +
              pls.produced._1 + " at efficiency " + frac +
              " and " + (unit_cost/100).toInt + "/unit.");
        }
        else {
          lost_runs_cost += total_cost;

          if(! GLOBAL.silent)
            println(o + " had a production line with zero efficiency.");
        }
        //      log = (get_time, frac) :: log;
      }
    )

  }
}

