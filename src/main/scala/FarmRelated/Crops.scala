package farmrelated.crop

import Simulation.Factory._
import code._
import Owner._
import Simulation._
import Securities.Commodities._
import landAdministrator.LandOverlay
import farmpackage.Farm

/** Add an access to a landOverlay, in order to compute unit produced, and the fact that crops should influence soil (quality, dryness)*/
class CropProductionLine(
  _lOver: LandOverlay,
  pls: ProductionLineSpec,
  o: Farm,
  salary: Int,
  start_time: Int,
  //goodwill : Double = 0.0,
  //lost_runs_cost : Double = 0.0,
  private var rpt : Int = 0,
  private var frac : Double = 1.0,
  private var costs_consumables : Double = 0.0) extends ProductionLine(pls,o,salary,start_time) {

    var Co2Emitted: Double = 0.0
    var fertilized: Boolean = false 
    var efficiency: Double = 1.0
    var lOver: LandOverlay = _lOver

    //Used to compute average price in Farm
    var unitPrice: Double = 0
    var quantity: Int = 0

    //Used to quantify efficiency of boosters use
    private var boosterFrac: Double = 1.0

    /** Compute the performance of the production line. Influenced by product used, ground quality
      * 1.0 means normal productivity. 
      * @return the performance of the production Line
      */
    def efficiencyFunc: Double = {
      //These values are random. Find true statistics afterward
      if(fertilized){
        efficiency = Math.min(efficiency + 0.2, 1.5)
      }
      else{
        efficiency = Math.max(efficiency - 0.2, 1.0)
      }
      //efficiency * lOver.soilQuality * frac
      frac
    }

    override def algo = __forever(
      __do { // start of production run
        costs_consumables = 0;
        //print("buying consumables: " + o + " " + this + ". ");
        frac = 1.0
        boosterFrac = 1.0
        
        //wait until there are wheet seeds to start production
        //__dowhile(
        //  __do{
        //    for(x <- pls.consumed) {
        //      val n = math.min(o.available(x._1), x._2); // requested and available
        //      println("Removing the consumed product:"  + x._1 + " in quantity: " + n)
        //      costs_consumables += o.destroy(x._1, n);
        //      frac = math.min(frac, n.toDouble / x._2);
        //    }
        //  },
        //  __wait(1)
        //)({o.available(WheatSeeds) == 0})

        for(x <- pls.consumed) {
          val n = math.min(o.available(x._1), x._2); // requested and available
          costs_consumables += o.destroy(x._1, n);
          frac = math.min(frac, n.toDouble / x._2);
        }

        //using the boosters
        pls.boosters match {
          case Some(list) => {
            for(booster <- list) {
              val n = math.min(o.available(booster._1), booster._2); // requested and available
              costs_consumables += o.destroy(booster._1, n);
              // min ratio over all boosters. Improve by separating different ratios of available vs needed of each booster
              boosterFrac = math.min(boosterFrac, n.toDouble / booster._2)
            }
          }
          case None => {} //nothing more to do 
        }
          
        goodwill = costs_consumables;
        if((frac < 1.0) && (! GLOBAL.silent))
          println(o + " " + " starts low-efficiency run.");

        rpt = 0;
      },
      __dowhile(
        __wait(1),
        __do{
          //print("paying salaries. ");
          // salaries are paid globally (by the factory)
          goodwill += pls.employees_needed * salary;
          rpt += 1;
        }
      )({ rpt < pls.time_to_complete }),
      __do{
        //print("production complete! ");

        //Multiply between them the increase of productivity of all boosters,
        val boosterRatio = pls.boosters match {
          case Some(list) => {
            list.foldLeft(1.0){(acc, booster) => acc * booster._3} * boosterFrac
          }
          case None => 1.0
        }
        val units_produced = (pls.produced._2  * efficiencyFunc * Math.max(1,boosterRatio)).toInt; // here to influence quantity produced 
        val personnel_costs = pls.employees_needed * salary *
                              pls.time_to_complete;
        val total_cost : Double = costs_consumables + personnel_costs;
        val unit_cost = total_cost / units_produced;

        
        unitPrice = unit_cost
        quantity = units_produced

        //It will be reset by the farm(owner) once taken into account
        Co2Emitted += lOver.getSurface * CONSTANTS.KG_CO2_PER_WHEAT_CROP_HA

        if(units_produced > 0) {
          o.make(pls.produced._1, units_produced, unit_cost);

          if(! GLOBAL.silent)
          println(o + " produces " + units_produced + "x " +
            pls.produced._1 + " at efficiency " + frac +
            " and " + (unit_cost/100).toInt + "/unit.");

          //test of holding ressources: after should call an holding strategy from the farm
          
          //Hold all the wheat, then call selling strategy in farm to decide when to sell it
          pls.produced._1 match {
            case Wheat => {
              o.holdCommodity(Wheat, units_produced, Some(o.s.timer + CONSTANTS.WHEAT_EXPIRY_TIMER_IN_MONTH))
              o.toSellEachTurn.update(pls.produced._1, o.toSellEachTurn.getOrElse(pls.produced._1,0) + units_produced)
            }
            case Fertilizer => {
              //Put half of fertilizer on the market, remaining is for own use
              o.holdCommodity(Wheat, units_produced/2, Some(o.s.timer + CONSTANTS.FERTILIZER_EXPIRY_TIMER_IN_MONTH))
              //o.toSellEachTurn.update(pls.produced._1, o.toSellEachTurn.getOrElse(pls.produced._1,0) + units_produced)
            }
            case _ => println("This type of crop is unknown")
          }
          

          //if(o.sellToCoopWorth(pls.produced._1)){
          //  o.sellFromCoop(List((pls.produced._1, units_produced - keepForFarmUse(pls.produced._1, units_produced))))
          //}
          
          //else nothing to do, the farm will sellby itself
          
        }
        else {
          lost_runs_cost += total_cost;

          if(! GLOBAL.silent)
          println(this + " had a production line with zero efficiency.");
        }
//        log = (get_time, frac) :: log;
      }
    )


    private def keepForFarmUse(com: Commodity, units_produced: Int): Int = {
      var ownUse: Int = 0
      if(pls.produced._1 == com){
        o.crops.foreach(crop => {
            crop.pls.consumed.filter(elem => elem._1 == com) match {
              case head :: next => ownUse = Math.min(ownUse + head._2, units_produced)
              case Nil => {}
            }
        })
      }
      ownUse
    }


  }