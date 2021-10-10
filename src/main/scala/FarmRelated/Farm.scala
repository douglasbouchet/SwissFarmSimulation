package farmpackage {

  import Securities.Commodities._
  import Securities._
  import Simulation._
  import _root_.Simulation.Factory._
  import code._
  import farmrelated.Herd
  import farmrelated.cooperative.AgriculturalCooperative
  import farmrelated.crop.CropProductionLine
  import glob._
  import geography.{CadastralParcel, City, Crop, LandAdministrator, LandOverlay, Location, LocationAdministrator, Paddock}
  import market.Prices

  import scala.collection.mutable


  case class Farm(s: Simulation, obs: Observator, prices: Prices, landAdmin: LandAdministrator) extends SimO(s) with Location {

    var parcels: List[CadastralParcel] = List()
    var landOverlays: List[LandOverlay] = List()
    var crops: List[CropProductionLine] = List[CropProductionLine]()
    var paddocks: List[Paddock] = List[Paddock]() // TODO don't forget to update this if reassigning landoverlay to other purpose
    var herds: List[Herd] = List[Herd]()
    var cooperative: Option[AgriculturalCooperative] = None
    //This are assigned by the generator atm (some random district/cities name atm)
    override var city: City = _

    protected var hr: HR = HR(s, this)

    //Strategy for selling
    var prevPrices: scala.collection.mutable.Map[Commodity, Double] = scala.collection.mutable.Map[Commodity, Double]()
    var toSellEachTurn: scala.collection.mutable.Map[Commodity, Int] = scala.collection.mutable.Map[Commodity, Int]()

    var cropRotationSchedule: mutable.Map[CropProductionLine, List[Commodity]] = collection.mutable.Map[CropProductionLine,List[Commodity]]()

    def addParcels(newParcels: List[CadastralParcel]): Unit = {
      parcels :::= newParcels
    }

    /** For each dummy, make an average over all crops that produces this dummy */
    override def price(dummy: Commodity): Option[Double] = {
      //println(s"The actual price for com $dummy is : " + 1.05 * inventory_avg_cost.getOrElse(dummy, 0.0))
      //println("The market price is : " + s.prices.getPriceOf(dummy))
      if (crops.nonEmpty && (saleableUnits(dummy) > 0))
        Some(1.05 * inventory_avg_cost.getOrElse(dummy, 0.0))

      else None
    }

    //private def changeActivity(newState: Boolean, prodL: ProductionLine) {prodL.active = newState}

    override def stat: Unit = {
      //println(this + " " + inventory_to_string())

      //Voir combien on pait pour les resources car 40000 enormes ?
      println(this + " capital = " + capital/100 + "  " + inventory_to_string)
    }

    //TODO add here the fact to change the type of agriculture 
    override def algo: __forever = __forever(
      __do {
        println("Potential candidates:" + landAdmin.findNClosestFarmers(parcels(0), 2))
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
      },
      __wait(1),
      __do {
        assert(hr.employees.length == crops.map(_.pls.employees_needed).sum +
          herds.foldLeft(0){(acc, num) => acc + num.cows.map(cow => cow.pls.employees_needed).sum })
        hr.pay_workers()
        removeExpiredItems(s.timer)
        sellingStrategy //manage hold commodities
      },
    )

    override def mycopy(
        _shared: Simulation,
        _substitution: mutable.Map[SimO, SimO]
    ): SimO = ???

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

      //Place this farm inside a city chosen randomly
      city = LocationAdministrator.cities(scala.util.Random.nextInt(LocationAdministrator.cities.length))

      //We only populate 1 paddock with animals, and keep the others paddock empty, in order to put animals inside them when their current paddock is out of grass
      var paddockOccupied: Boolean = false

      landOverlays.foreach {
        case lOver@(_: Paddock) => {
          if(!paddockOccupied){
            val herd: Herd = new Herd(this, lOver, 20, hr.salary) //TODO check ca
            herd.initHerd()
            herds ::= herd
            hr.hire(1)
            paddockOccupied = true
          }
          paddocks ::= lOver

        }
        case lOver@(crop: Crop) => {
          //afterwards we could add more complex attributes for productivity
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
            CONSTANTS.WHEAT_PROD_DURATION,
            Some(List((Fertilizer, 10, 1.20)))
          )
          hr.hire(worker)
          val prodL = new CropProductionLine(lOver, prodSpec, this, hr.salary, s.timer)
          crops ::= prodL
          s.market(prodSpec.produced._1).add_seller(this)

          //add the basic crop rotation schedule
          cropRotationSchedule.put(prodL, List(Pea, CanolaOil, Wheat, Wheat))
        }
        case _ => {} //we already did above, implement with crop when done
      }
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

    override def run_until(until: Int): Option[Int] = {
      // this ordering is important, so that bulk buying
      // happens before consumption.
      val nxt1 = super.run_until(until).get
      if (crops.isEmpty && herds.isEmpty){
        Some(nxt1)
      }
      else if(crops.isEmpty && herds.nonEmpty){
        Some(math.min(nxt1, herds.map(herd => herd.cows.map(_.run_until(until).get).min).min))
      }
      else if(crops.nonEmpty && herds.isEmpty){
        Some(math.min(nxt1, crops.map(_.run_until(until).get).min))
      }
      else{
        Some(math.min(nxt1, math.min(herds.map(herd => herd.cows.map(_.run_until(until).get).min).min, crops.map(_.run_until(until).get).min)))
      }
    }

    /** The commodities asks may not be available immediately
     * order is pass to the coop, which sells them back to this when products are buy by coop */ 
    def buyMissingFromCoop(toBuy: List[(Commodity, Int)]): Unit = {
          cooperative match {
            case None => println("Farm: " + this + "should be part of a cooperative to buy from it")
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
        case None => println("Farm: " + this + "should be part of a cooperative to sell from it")
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
        0.98*prices.getDomesticPricesOf(com)
      }

      /** 1st Milestone: estimate profits/loss randomly (between 90 and 110 %)
       * 2nd Milestone: estimate profits/loss based on an expected value of how many and at which price stock is sell on market
       * A bit like someone who would speculate 
       * //TODO see if can add some contracts to ensure selling price ? 
       * */
      def getSelfPrice: Double = {
        (90.0 + scala.util.Random.nextInt(20))/100 * prices.getDomesticPricesOf(com)
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
    def sellingStrategy(): Unit = {
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
                    if(prices.getDomesticPricesOf(commodity) <= prevPrices.getOrElse(commodity,0.0)){
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
                prevPrices.put(commodity, prices.getDomesticPricesOf(commodity))
            }
          }
    }

    /* Using the coop if part of to chose which will be the next production. Should be called once prev crop production ended
     * Just select next crop in a round robin way
     * Afterwards, more complex decision making (impact on ground, market demand)
     */
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


    def resetCropsAndHerdsEmissions(): Unit = {
      crops.foreach(_.Co2Emitted = 0.0)
      herds.foreach(_.cows.foreach(cow => {cow.methane = 0.0; cow.ammonia = 0.0}))
    }

    def updateCropsAndHerdsEmissions(): Unit = {
      obs.year_co2 += crops.map(_.Co2Emitted).sum
      obs.year_methane += herds.map(_.cows.map(_.methane).sum).sum
      obs.year_ammonia += herds.map(_.cows.map(_.ammonia).sum).sum
      resetCropsAndHerdsEmissions()
    }

    def sendInvCost(com: Commodity): Double = {
      inventory_avg_cost.getOrElse(com, 0.0)
    }

    

    def canEqual(a: Any): Boolean = a.isInstanceOf[Farm]

    override def equals(that: Any): Boolean =
      that match {
          case that: Farm => {
              that.canEqual(this) &&
              this.parcels == that.parcels
              //See if add more comparisons
          }
          case _ => false
    }
  }
}