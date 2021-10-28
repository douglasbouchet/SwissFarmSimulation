
package FarmRelated
import Securities.Commodities._
import Securities._
import Simulation._
import geography.LandOverlayPurpose

import scala.List
//import _root_.Simulation.Factory._
import modifyFromKoch.{HR, ProductionLine, ProductionLineSpec}
import code._
import glob._
import geography.{CadastralParcel, City, Crop, LandAdministrator, LandOverlay, Location, LocationAdministrator, Paddock}
import market.Prices
import glob.Observator
import CONSTANTS._

import scala.collection.mutable
import Companies.Production


class Farmer(_s: Simulation, _obs: Observator, _landAdmin: LandAdministrator, _age: Int, _children: List[Child]) extends SimO(_s) {
    var parcels: List[CadastralParcel] = List()
    var landOverlays: List[LandOverlay] = List()
    var cooperative: Option[AgriculturalCooperative] = None
    val obs = _obs
    val s = _s


    //Strategy for selling
    var prevPrices: scala.collection.mutable.Map[Commodity, Double] = scala.collection.mutable.Map[Commodity, Double]()
    var toSellEachTurn: scala.collection.mutable.Map[Commodity, Int] = scala.collection.mutable.Map[Commodity, Int]()
    var cropRotationSchedule: mutable.Map[CropProductionLine, List[Commodity]] = collection.mutable.Map[CropProductionLine,List[Commodity]]()


    var landAdmin: LandAdministrator = _landAdmin
    val children: List[Child] = _children
    var age: Int = _age
    var prevIncomes: scala.collection.mutable.Map[Commodity, Double] = scala.collection.mutable.Map[Commodity, Double]()
    var municipality : List[Farmer] = List() //TODO fix, create a class instead of a list of farmer
    //head = last year, second = 2 years before,...*/
    var last5HouseHoldIncomes: List[Int] = List[Int](0,0,0,0,0)

    var productions: List[Production] = List()
    var prices: scala.collection.mutable.Map[Commodity, Double] = scala.collection.mutable.Map[Commodity, Double]()
    var totalCostPerCom: scala.collection.mutable.Map[Commodity, Double] = scala.collection.mutable.Map[Commodity, Double]()
    var relatedCommodities: List[List[Commodity]] = List[List[Commodity]]()

    //var bank: Bank
    //var tools: List[someStuff]


    //TODO might be useless, replaced by a better method for land leasing
    def addParcels(newParcels: List[CadastralParcel]): Unit = {
      parcels :::= newParcels
    }

    /** For each dummy, make an average over all crops that produces this dummy */
    //TODO there should be one method to tell the company that production is ready, with only inventory avg cost,
    //and one that accept a price proposed by a company
    override def price(dummy: Commodity): Option[Double] = {
      //println(s"The actual price for com $dummy is : " + 1.05 * inventory_avg_cost.getOrElse(dummy, 0.0))
      //println("The market price is : " + s.prices.getPriceOf(dummy))
      //if (crops.nonEmpty && (saleableUnits(dummy) > 0))
      //  Some(1.05 * inventory_avg_cost.getOrElse(dummy, 0.0))
      //else None
      Some(0)
    }

    //private def changeActivity(newState: Boolean, prodL: ProductionLine) {prodL.active = newState}

    override def stat: Unit = {
      //println(this + " " + inventory_to_string())

      //Voir combien on pait pour les resources car 40000 enormes ?
      println(this + " capital = " + capital/100 + "  " + inventory_to_string)
    }

    //TODO Will be replaced by behave
    //override def algo: __forever = _*_forever(
      /*
      __do {
        //println("Potential candidates:" + landAdmin.findNClosestFarmers(parcels(0), 2))
        //Each turn, get the emissions of each crop/herd
        updateCropsAndHerdsEmissions()

        crops.foreach(crop => {
          //if there is a cooperative, buy from it. Else by itself
          val boostersToBuy = crop.pls.boosters match {
            case Some(list) => list.map(elem => (elem._1, elem._2))
            case None => List() 
          }
          cooperative match {
            case Some(_) => buyMissingFromCoop(crop.pls.consumed ++ boostersToBuy)
            case None => bulk_buy_missing(crop.pls.consumed ++ boostersToBuy, 1)
          }

          //if quality of soil is not to low, we can use fertilizer
          //if(crop.lOver.soilQuality > 1.0) fertilize(crop)
          //else fertilize(crop, state = false)
          //Update the state of the ground to impact it according to actions taken
          //if(crop.fertilized) crop.lOver.soilQuality = Math.max(crop.lOver.soilQuality - 0.03, 0.5) 
          //else crop.lOver.soilQuality = Math.min(crop.lOver.soilQuality + 0.02, 1.0)
        })


        //Buy the necessary stuff for herds if needed
        herds.foreach(herd => {
          //There is no more grass on the current Paddock, so buy for 1 month of grass for the all herd. Buy should only happen if only one paddock is available
          //TODO  a problem might occur if cows consumption > prod of all paddocks -> #grass will tend to 0. And this code might not scale well if multiple herds are presents
          //Fix this next (maybe add condition on expected consumption or only buy few amounts of grass each time (to last one week))
          if(herd.newGrassOrdered && paddocks.length == 1){
            assert(herd.cows.nonEmpty)
            cooperative match {
              case Some(_) => buyMissingFromCoop(List((Grass, herd.cows.length * herd.cows.head.dailyGrassCons * 30 / CONSTANTS.TICKS_TIMER_PER_DAY)))
              case None => bulk_buy_missing(List((Grass, herd.cows.head.dailyGrassCons * 30 / CONSTANTS.TICKS_TIMER_PER_DAY)),  herd.cows.length)
            }
            herd.newGrassOrdered = false
          }
        })
        assert(hr.employees.length == crops.map(_.pls.employees_needed).sum +
          herds.foldLeft(0){(acc, num) => acc + num.cows.map(cow => cow.pls.employees_needed).sum })
        hr.pay_workers()
        removeExpiredItems(s.timer)
        sellingStrategy //manage hold commodities
      },
      __if(s.timer % 365*CONSTANTS.TICKS_TIMER_PER_DAY == 0){
        __do{
          //Each year, check if a farmer needs to retire, and if so, handle it
          farmerExiting
          age += 1
        }
      },
      __wait(1)

       */
    //)

    override def mycopy(
        _shared: Simulation,
        _substitution: mutable.Map[SimO, SimO]
    ): SimO = ???

    //SHould be useless now
    /*
    def addCrop(crop: Crop): Unit = {
      val area: Double = crop.getSurface
      val nWorker = math.round((area / CONSTANTS.HA_PER_WORKER).toFloat)
      val worker = if (nWorker > 0) nWorker else 1
      CONSTANTS.workercounter += worker
      val prodSpec: ProductionLineSpec = ProductionLineSpec(
        worker,
        List(/** (
         * WheatSeeds,
         * (area * CONSTANTS.WHEAT_SEEDS_PER_HA).toInt) */),
        List(
          (WheatSeeds, (area * CONSTANTS.WHEAT_SEEDS_PER_HA).toInt

            /** * 1000 */
          ),
        ),
        (
          Wheat,
          (area * CONSTANTS.WHEAT_PRODUCED_PER_HA).toInt
        ),
        CONSTANTS.CROP_PROD_DURATION.getOrElse(Wheat, 1000),
        Some(List((Fertilizer, 10, 1.20)))
      )
      hr.hire(worker)
      val prodL = new CropProductionLine(crop, prodSpec, this, hr.salary, s.timer)
      crops ::= prodL
      s.market(prodSpec.produced._1).add_seller(this)

      //add the basic crop rotation schedule
      cropRotationSchedule.put(prodL, List(Pea, CanolaOil, Wheat, Wheat))
    }

    */
    /** Create a factory for each landOverlay of purpose the farm has
      * ProductionLineSpec is determined in function of area, and purpose of
      * LandOverlay
      * @note
      *   ProductionLineSpec number are chosen randomly for the moment e.g a
      *   worker can handle at most 5ha of crops
      *   https://donnees.banquemondiale.org/indicateur/AG.YLD.CREL.KG -> 1 area
      *   produces 6tonnes of wheat 1 area of wheat needs 0.15 tonnes of
      *   wheatSeeds These numbers should be updated afterwards
      */
    def init(): Unit = {
      landAdmin.addAgent(this)
      //give some capital + commodities to start
      capital += 20000000
      make(WheatSeeds, 1300, 10)
      make(Fertilizer, 7, 2)
      landOverlays.foreach(lOver =>
        productions ::= instantiateProductionFromLandOverlay(lOver)
      )

      val x = 2
      //TODO also rethink all the init function, should only add some basic capital/material, as land overlays
      //use is now call if they are empty (i.e no purpose)
      /*
      //We only populate 1 paddock with animals, and keep the others paddock empty, in order to put animals inside them when their current paddock is out of grass
      var paddockOccupied: Boolean = false

      landOverlays.foreach {
        case lOver@(_: Paddock) => {
          if(!paddockOccupied){
            val herd: Herd = new Herd(this, lOver, 20, hr.salary)
            herd.initHerd()
            herds ::= herd
            hr.hire(1)
            paddockOccupied = true
          }
          paddocks ::= lOver

        case lOver@(crop: Crop) =>
          addCrop(crop: Crop)

        case _ => {} //we already did above, implement with crop when done
      }
       */
    }

    /** Returns whether everything was successfully bought. */
    def bulk_buy_missing(
        _l: List[(Commodity, Int)],
        multiplier: Int
    ): Boolean = {
      val l = _l.map(t => {
        // DANGER: if we have shorted his position, this amount is
        // not sufficient.
        val amount = math.max(0, t._2 * multiplier - available(t._1))
        (t._1, amount)
      })

      def successfully_bought(line: (Commodity, Int)) = {
        val alreadyBuyFrom = contactNetwork.contactsSellingCom(line._1)
        //val alreadyBuyFrom = contactNetwork.contacts.map(_._1).toList
        //println("Already buy to")
        s.market(line._1).market_buy_order_now(s.timer, this, line._2,alreadyBuyFrom) == 0
      }
      l.forall(successfully_bought)
    }

    /** The commodities asks may not be available immediately
     * order is pass to the coop, which sells them back to this when products are buy by coop */ 
    def buyMissingFromCoop(toBuy: List[(Commodity, Int)]): Unit = {
          cooperative match {
            case None => println("Farmer: " + this + "should be part of a cooperative to buy from it")
            case Some(coop) =>
              toBuy.foreach{
                case(com: Commodity, unit: Int) => (
                  //Change this line
                  coop.buyLogs.update(this,
                    coop.buyLogs(this) ++ Map(com -> (coop.buyLogs(this).getOrElse(com, 0) + math.max(0, unit - available(com)))))
                )
              }
          }
        }

    def sellFromCoop(toSell: List[(Commodity, Int)]): Unit = {
      cooperative match {
        case None => println("Farmer: " + this + "should be part of a cooperative to sell from it")
        case Some(coop) =>
          toSell.foreach{
            case(com: Commodity, unit: Int) =>
              assert(coop.saleableCommodities.contains(com))
              coop.sellLogs.update(this, coop.sellLogs(this) :+ (com, unit))
              sell_to(s.timer, coop, com, unit)
          }
      }
    }

    /** Chose for a given commodity between selling to cooperative or on its own to make the maximum money 
     * @return true if selling with coop is worth, else false */
    def sellToCoopWorth(com: Commodity): Boolean = {
      //For the moment, as coop sells in gross, price are a bit lower than the ones on the market.
      //Worthness of selling to coop is getting money instantly + sure to sell all at an okay price
      def getCoopPrice: Double = {
        //0.98*s.prices.getPriceOf(com)
        //0.98*prices.getDomesticPricesOf(com)
        -1
      }

      /** 1st Milestone: estimate profits/loss randomly (between 90 and 110 %)
       * 2nd Milestone: estimate profits/loss based on an expected value of how many and at which price stock is sell on market
       * A bit like someone who would speculate 
       * //TODO see if can add some contracts to ensure selling price ? 
       * */
      def getSelfPrice: Double = {
        // (90.0 + scala.util.Random.nextInt(20))/100 * prices.getDomesticPricesOf(com)
        -1
      }
      
      cooperative match {
        case None => false // Will sell by itself, see if some stocks needs to be hold
        case Some(_) => getCoopPrice > getSelfPrice
        
      }
    }

    /**
      * Implement a Fo Moo strategy (copy the others). If price is bearing(falling), sell. Else hold
      * If one turn remains before the held commodities expires, sell all
      */
    //TODO replace it by a simple selling to the most offering company
    def sellingStrategy(): Unit = {
      /*
      holdedCommodities.foreach{
              case (com: Security, (units:Int, endTimer:Option[Int])) => {
                val commodity = com.asInstanceOf[Commodity]
                endTimer match {
                  case Some(expireTimer) => {
                    if(s.timer < expireTimer - 1){
                        //Check if price has fallen
                        if(prices.getDomesticPricesOf(commodity) <= prevPrices.getOrElse(commodity,0.0)){
                          var toSell: Int = toSellEachTurn.getOrElse(commodity,0)/10
                          //avoid selling less than 10% of the stock each turn
                          if(holdedCommodity(com) < 2*toSell){
                            //Sell all remaining stock
                            toSell = holdedCommodity(com)
                          }
                          toSell = Math.min(holdedCommodity(com), toSell) // new
                          if(sellToCoopWorth(commodity)){
                            sellFromCoop(List((commodity, toSell)))
                          }
                          else{
                            releaseToMarket(commodity, toSell)
                          }
                        }
                      }
                    //One turn remains before being expired, sell all of this commodity
                    else if (s.timer == expireTimer - 1){
                      //Clear the hold inventory
                      //TODO PUT THIS INSIDE A METHOD ??
                      if(sellToCoopWorth(commodity)){
                        sellFromCoop(List((commodity, units)))
                      }
                      else{
                        releaseToMarket(commodity, units)
                      }
                      //END TODO
                      //Otherwise, as removed from held, could now be buy by everyone
                      toSellEachTurn.put(commodity, 0)
                    }
                  }
                  case None => {
                    //TODO duplicated code with just above, create methode
                    //In that case, the commodity was in the inventory, so just sell as much as we want.
                    if(prices.getDomesticPricesOf(commodity) <= prevIncomes.getOrElse(commodity,0.0)){
                      val toSell: Int = Math.max(saleableUnits(com), 0)
                        //var toSell: Int = toSellEachTurn.getOrElse(commodity,0)/10
                        //avoid selling less than 10% of the stock each turn
                        //if(holdedCommodity(com) - toSell < toSell){
                        //  //Sell all remaining stock
                        //  toSell = holdedCommodity(com)
                        //}
                        //val quantityToSell = Math.min(holdedCommodity(com), toSell)
                        //releaseToMarket(commodity, quantityToSell)
                        if(sellToCoopWorth(commodity)){
                          //sellFromCoop(List((commodity, quantityToSell)))
                          sellFromCoop(List((commodity, toSell)))
                        }
                    }
                  }
                }
                
                //update prevPrice of commodity
                prevIncomes.put(commodity, prices.getDomesticPricesOf(commodity))
            }
          }
       */
    }

    /* Using the coop if part of to chose which will be the next production. Should be called once prev crop production ended
     * Just select next crop in a round robin way
     * Afterwards, more complex decision making (impact on ground, market demand)
     */
    //TODO should be adapted to be used after call to oracle
  /*
    def choseNextCrops(crop: CropProductionLine): Option[Commodity] = {
      cropRotationSchedule.get(crop) match {
        case Some(schedule) => {
          val prevCrop = schedule.head
          cropRotationSchedule.put(crop, schedule.tail :+ prevCrop)
          Some(prevCrop)
        }
        case _ => {
          println("No schedule was found for this crop")
          None
        }
      }
    }
   */

    //TODO this function might become useless
    def resetCropsAndHerdsEmissions(): Unit = {
      //crops.foreach(_.Co2Emitted = 0.0)
      //herds.foreach(_.cows.foreach(cow => {cow.methane = 0.0; cow.ammonia = 0.0}))
    }

    //TODO this one might be adapted
    def updateCropsAndHerdsEmissions(): Unit = {
      //obs.year_co2 += crops.map(_.Co2Emitted).sum
      //obs.year_methane += herds.map(_.cows.map(_.methane).sum).sum
      //obs.year_ammonia += herds.map(_.cows.map(_.ammonia).sum).sum
      resetCropsAndHerdsEmissions()
    }

    def sendInvCost(com: Commodity): Double = {
      inventory_avg_cost.getOrElse(com, 0.0)
    }
    //------methods for handling polluting emission--------------

    //should iterate over all landOverlays and pollute in function of their type (i.e WheatCrop, Paddock,....) TODO
    /** See how to define the method */
    def pollute(): Unit = ???

    //-----------------------------------------------------------

    //------methods for handling production of commodities-------

    /**  Instantiate a new Production
     *  Consumed & Produced are based on the MAP que tu viens de définir
     */
    def instantiateProductionFromLandOverlay(lOver : LandOverlay): Production = {
      val prod = new Production(s, this, 1, 1000, PROD_MAP(lOver.purpose)._1, PROD_MAP(lOver.purpose)._2,
                                ACTIVITIES_PROD_DURATION(lOver.purpose), landOverlay = Some(lOver))
      productions = prod :: productions
      prod
      //TODO not correct, the produced quantities should depend on the area of the land Overlay
    }

    /** call the payWorkers method of each Companies.Production */
    def payWorkers(): Unit = ???

    /** For each Production, call the getProduction method (handle creation of new commodities in case production ended
     * + dying of the production)
     * Should be call each epoch inside "algo"*/
    def updateProductions(): Unit = {
      productions.foreach(p => {
        if (!p.getProduction)
          p._produced.map(x => totalCostPerCom(x._1) = inventory_total_cost(x._1))

      }) //TODO getProd ret boolean commea ca on remove the productions après ce call)
      
    }

    //Should be called at the end of every year (as work for SwissLand).
    def updatePrice(): Unit = {
      //useful to not update multiple time price of commodities of the same type
      var increasedCommodities: List[Commodity] = List[Commodity]()
      all_commodities.foreach((com: Commodity) => {
        if (inventory.contains(com)) {
          val prevBenefits = computeBenef(com)
          val margin = 20.0 / 100 //TODO pass it as a constant, easier to change policy
          val sameCommType: List[Commodity] = relatedCommodities.filter(ls => ls.contains(com)).flatten
          //increase price of each concurrent commodity by margin%. If no previous price in prices, gives inventory avg cost
          if (prevBenefits > 0 || inventory(com) == 0 && sameCommType.exists(increasedCommodities.contains)) {
            increasedCommodities ::= com
            sameCommType.foreach((c: Commodity) =>
              prices.put(c, prices.getOrElse(c, inventory_avg_cost.getOrElse(c, 0.0)) * (1 + margin)))
          }
          //we should only decrease price if not everything was sold
          else if (inventory(com) > 0  && sameCommType.exists(increasedCommodities.contains))
            increasedCommodities ::= com
            sameCommType.foreach((c: Commodity) =>
              prices.put(c, prices.getOrElse(c, inventory_avg_cost.getOrElse(c, 0.0)) * (1 - margin)))

          //TODO do we reset the inventory average cost ?
        }
      })


    }


    //Initialise commodity prices, related commodities, land purpose,...
    def initialise(): Unit = ???

    def computeBenef(com: Commodity): Double = prevIncomes.getOrElse(com, 0.0) - totalCostPerCom.getOrElse(com, 0.0)


  //-----------------------------------------------------------

    /** This function should be called each epoch
     * In case some LandOverlays don't have any purpose, call the oracle to decide what to do with these LandOverlays
     * @param n: Int, once this number of LandOverlays without purpose is reached, call the oracle
     * */
    def callOracle(n: Int): List[A] = {
        val withoutPurpose = landOverlays.filter(_.purpose == LandOverlayPurpose.noPurpose)
        val budget = capital //TODO maybe not use all capital
        //if(withoutPurpose.length > n) getOracleStrategy(budget, withoutPurpose)
      List()
    }

    type A <: LandOverlay
    //TODO this needs tests
    //def getOracleStrategy(budget: Double, landResources: List[CadastralParcel]) : List[A] = ???
    def chooseNextProduction: List[LandOverlay] = {
      //iterate over unusedLOver, for each produced commodity, get its benefits
      var producedCommodities: List[Commodity] = List[Commodity]()
      landOverlays.filter(_.purpose == LandOverlayPurpose.noPurpose).foreach((lOver: LandOverlay) => {
        val res : (List[(Commodity, Int)], List[(Commodity, Int)]) = PROD_MAP.getOrElse(lOver.purpose, (List(), List()))
        if(res != (List(), List())){
          producedCommodities ::= res._2.head._1
        }
        else{
          println("The asked LandOverlayPurpose " + lOver.purpose + " does not exists")
        }
      })
      assert(producedCommodities.length == producedCommodities.distinct.length)
      //Order the commodity by decreasing benef (i.e max benef first)
      val orderedByBenef: List[(Commodity, Double)] = producedCommodities.map(com => (com, computeBenef(com))).sortBy(- _._2)
      //If we have at least 2 landOverlays, we give one parcel of the worst to the best
      if(orderedByBenef.length >= 2){
        //we increase production of best commodity by 20%, and reduce the production of worst commodity according to this.
        val bestLandOverlay : LandOverlay = landOverlays.filter(_.prevPurpose == CONSTANTS.COMMODITY_TO_LAND_OVERLAY_PURPOSE.getOrElse(orderedByBenef.head._1, null)).head
        val worstLandOverlay: LandOverlay = landOverlays.filter(_.prevPurpose == CONSTANTS.COMMODITY_TO_LAND_OVERLAY_PURPOSE.getOrElse(orderedByBenef.last._1, null)).head
        //val toIncreaseArea: Double        = math.min(bestLandOverlay.getSurface * 0.20, worstLandOverlay.getSurface)
        bestLandOverlay.landsLot = bestLandOverlay.landsLot :+ (worstLandOverlay.landsLot.head.asInstanceOf[CadastralParcel],  1.0)
        worstLandOverlay.landsLot = worstLandOverlay.landsLot.tail
        if(worstLandOverlay.landsLot.length == 0){
          //In that case, we remove the worst land Overlay and give all its remaining parcel to the best landOverlay
          _landAdmin.removeLandOverlay(worstLandOverlay)
          landOverlays = landOverlays.filterNot(_ == worstLandOverlay)
        }
      }
      //next we return landOverlays with the same purpose as their prevPurpose TODO can be complexified after
      val newLandOverlays: List[LandOverlay] = landOverlays.filter(_.purpose == LandOverlayPurpose.noPurpose)
      newLandOverlays.foreach((lOver: LandOverlay) => {
        lOver.purpose = lOver.prevPurpose
      })
      newLandOverlays
    }


    //---Methods for land leasing due to exiting---------

    /** This should be called every year, decides if the farmer needs to exit, and handle the inheriting of the farm
     * i.e transferring to a child or try to sell to other farmers */
    def farmerExiting(): Unit = {

      /**
       * Same logic as SwissLand
       * @return true if past 5 years household incomes were negative or farmer older than 65
       */
      def shouldExit: Boolean =  age >= 65 || last5HouseHoldIncomes.forall(_ < 0)

      /**
       * Same logic as SwissLand
       * @return true if Farmer has a child, and household incomes are slightly above a regional average
       */
      def childShouldInherit: Boolean = children.nonEmpty && last5HouseHoldIncomes.head > 1.01 * regionalAverageHouseHoldIncomes

      /** Keeps all the herds, crops, contact network,... So just reset the age to 35 (assume) and give a child with probability 0.875 (respect probability used by SwissLand)*/
      def transferToChild: Unit = {
        //no problem like in Terminator where John Connor is older than Sarah Connor cause if exit before 65, this is because
        //house holds are negative, thus son will not take over (only if all regional incomes are negative but in this case there is a problem)
        age = CONSTANTS.CHILD_TAKE_OVER_AGE
        // TODO this will be modified children = scala.util.Random.nextFloat() < 0.875
      }

      /**
       * Reproducing selling mechanism used by SwissLand (i.e only ask the 5 closest farmers to lease the lands)
       * @return true if all parcels have been sold
       */
      def sellToOtherFarmer: Boolean = {
        def recSell(parcel: CadastralParcel, buyers: List[Farmer]): Boolean = {
          if(buyers.isEmpty) false
          else if(buyers.head.shouldByParcel(parcel, this)) {
            true
          } else recSell(parcel, buyers.tail)
        }
        //try to sell each parcel to a farmer
        if(parcels.nonEmpty){
          val buyers = landAdmin.findNClosestFarmers(parcels.head,5).getOrElse(List()).map(_._1)
          parcels.forall(recSell(_, buyers))
          }
        else {
          println("There is no parcels to sell, strange...")
          true
        }
      }

      /**
       * get the regional households incomes (e.g by iterating over all farmers located at less than 50km or whatever)
       * @return
       */
      def regionalAverageHouseHoldIncomes: Double = {
        val regionalFarmers = landAdmin.findNClosestFarmers(parcels.head, 20).getOrElse(List()).map(_._1)
        val lastIncomes = regionalFarmers.map(_.last5HouseHoldIncomes.head)
        lastIncomes.foldLeft(0.0)(_ + _) / lastIncomes.length
      }

      if (shouldExit) {
        if(childShouldInherit){
          transferToChild
        }
        else{
          if(!sellToOtherFarmer){
            print("All the parcels haven't been sold to other farmers, TODO what do we do in that case ? ")
          }
        }
      }
    }

    /** Sell the parcel to the buyer (i.e money transfer), change owner, and add/remove parcel inside parcels of buyer/seller*/
    def transferParcelFrom(parcel: CadastralParcel, farmer: Farmer): Unit = {
      val price = (parcel.area * CONSTANTS.M_SQUARE_PRICE).toInt
      capital += price
      farmer.capital -= price

      landAdmin.changeCadastralParcelOwner(parcel, farmer) //TODO check if parcel is well removed from landOverlay
      parcels = parcels.filter(_ != parcel)
      farmer.addParcels(List(parcel))
    }

    //TODO see how to implement this logic, for the moment, only buy if can market buy it i.e really greedy
    def shouldByParcel(parcel: CadastralParcel, from: Farmer): Boolean = {
      val price = parcel.area * CONSTANTS.M_SQUARE_PRICE
      if(capital > price){

        //if a farmer wants to buy the new land, sell it to him
        transferParcelFrom(parcel, from)
        //decide what will the farmer do with its parcel
        //TODO here we should implement a strategy (rmd decision for the moment)
        if(scala.util.Random.nextFloat() < 0.5) handleNewParcel(parcel, LandOverlayPurpose.wheatField)
        else handleNewParcel(parcel, LandOverlayPurpose.paddock)

      }
      capital > price

    }

    /** If the new purpose is a wheat field, we pick the head crop (if exist, and extend it with the new land)
     * If no crops exist, add a new one
     * If new purpose is a paddock, add the parcel as a new paddock to paddock's list
     * Meadow and no purpose, just add them to list of parcels atm*/

    def handleNewParcel(parcel: CadastralParcel, new_purpose : LandOverlayPurpose.Value): Unit = ???
      //TODO to adapt
    /*def handleNewParcel(parcel: CadastralParcel, new_purpose : LandOverlayPurpose.Value): Unit = {
      new_purpose match {
        case geography.LandOverlayPurpose.wheatField =>
          crops.headOption match {
            case Some(cropProdLine) =>
              landAdmin.removeLandOverlay(cropProdLine.crop)
              addCrop(landAdmin.addLandOverlay(cropProdLine.crop.landsLot :+ (parcel, 100), geography.LandOverlayPurpose.wheatField).asInstanceOf[Crop])
            case None => addCrop(landAdmin.addLandOverlay(List((parcel, 100)), geography.LandOverlayPurpose.wheatField).asInstanceOf[Crop])
          }
        case geography.LandOverlayPurpose.paddock =>
          paddocks ::= landAdmin.addLandOverlay(List((parcel, 100)), geography.LandOverlayPurpose.paddock).asInstanceOf[Paddock]

        case geography.LandOverlayPurpose.meadow =>
          addParcels(List(parcel))
          println("TODO IMPLEMENT ME")

        case geography.LandOverlayPurpose.noPurpose =>
          addParcels(List(parcel))
          println("TODO IMPLEMENT ME")

      }
    }
    */
    //----------------------------------------------------

    def sellToCompanies: Boolean = ???



    //----------------------------------------------------


  //--------Main algorithm for behavior of farmer-------

  /**
   * - 1 epoch = 1 month/day, can change
   *
   * The behavior of the farmer is the following,
   * For each epoch:
   *    - check if he must retire, and if so, either lease its land to farmer of its municipality, or his child(ren) takes over
   *    - update the productions (~ with counter and time to complete)
   *    - if commodities have been sold (say most parts)
   *       - call the oracle (Gams optimiser for profit), by giving its land resources (i.e lands he possess)
   *    and a budget (capital for the moment)
   *       - ?? decide if wants to be pass to an organic farming (this should influence its choice of land uses (i.e which crops to grow,
   *       have livestock, ...))
   *       - decide of the using of its land in function of response of oracle + personal choice
   *       - purchase the consumed commodities for its production
   *       - put itself on the seller market of commodities it will produce
   *
   * @note can we assume that the oracle could schedule multiple usage of a same land for the same year ?
   *       i.e growing in first half of the year wheat, and then leasing the land, or growing winter wheat,....
   * In that case, return oracle = List[(LandOverlay, List(LandOverlayPurpose))] i.e on parcel (1,2) -> wheat, winter wheat, leasing
   * And we can get the timing of each production with some constants
   */
  override def algo = __forever(
    __do{
      farmerExiting()
      updateProductions()
    },
    __if(s.timer % 364 == 0)(
      //at the end of each year, update prices base on selling performances
      __do{
        updatePrice()
      }
    ),
    __wait(30*CONSTANTS.TICKS_TIMER_PER_DAY)
  )
  //----------------------------------------------------



  def canEqual(a: Any): Boolean = a.isInstanceOf[Farmer]

    override def equals(that: Any): Boolean =
      that match {
        case that: Farmer => {
          that.canEqual(this) &&
            this.parcels == that.parcels
          //See if add more comparisons
        }
         case _ => false
      }

  
  def strategicComToBuy(): List[(Commodity, Double)] = {
      var ls = List[(Commodity, Double)]()
      //val overlays = getOracleStrategy(capital, parcels)
      val overlays = chooseNextProduction
      overlays.foreach(o => 
        PROD_MAP(o.purpose)._1.map(
          x => {ls :::= List((x._1, o.getSurface * x._2))}
        )
      )
    ls
  }

}

