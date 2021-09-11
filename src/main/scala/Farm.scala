package farmpackage {

  import Simulation._
  import Simulation.Factory._
  import landAdministrator.CadastralParcel
  import landAdministrator.LandOverlay
  import landAdministrator.LandOverlayPurpose._
  import code._
  import Securities.Commodities._
  import scala.collection.mutable
  import cooperative.AgriculturalCooperative
  import javax.lang.model.`type`.NullType
  import Securities._


  case class Farm(s: Simulation) extends SimO(s) {

    //type ITEM_T = Security

    var parcels: List[CadastralParcel] = List()
    var landOverlays: List[LandOverlay] = List()
    var crops: List[CropProductionLine] = List[CropProductionLine]()
    //var cattles: List[CropProductionLine] = List[CropProductionLine]() // TODO maybe change name of "Crop" by something
    //that fits cattles + crops
    var cooperative: Option[AgriculturalCooperative] = None 


    //use afterwards to model other co2 emission
    var Co2: Double = 0

    protected var hr: HR = HR(s, this)

    //Strategy for selling
    var prevPrices: scala.collection.mutable.Map[Commodity, Double] = scala.collection.mutable.Map[Commodity, Double]()
    var toSellEachTurn: scala.collection.mutable.Map[Commodity, Int] = scala.collection.mutable.Map[Commodity, Int]()

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

      //Voir combien on pait pour les ressources car 40000 enormes ?
      println(this + " capital = " + capital/100 + "  " + inventory_to_string)
    }

    //TODO add here the fact to change the type of agriculture 
    override def algo: __forever = __forever(
      __do {
        s.observator.Co2 += crops.map(_.Co2Emitted).sum

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
          //buyMissingFromCoop(crop.pls.consumed)
          crop.Co2Emitted = 0.0
          //if quality of soil is not to low, we can use fertilizer
          //if(crop.lOver.soilQuality > 1.0) fertilize(crop)
          //else fertilize(crop, state = false)
          //Update the state of the ground to impact it according to actions taken
          //if(crop.fertilized) crop.lOver.soilQuality = Math.max(crop.lOver.soilQuality - 0.03, 0.5) 
          //else crop.lOver.soilQuality = Math.min(crop.lOver.soilQuality + 0.02, 1.0)
        })
        
        //crops.foreach(crop => changeActivity(false, crop))
      },
      __wait(1),
      __do {
        assert(hr.employees.length == crops.map(_.pls.employees_needed).sum)
        hr.pay_workers()
        removeExpiredItems(s.timer)
        sellingStrategy //manage hold commodities
      }
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
      //give some capital to start
      capital += 20000000
      make(WheatSeeds, 3000, 10)
      make(Fertilizer, 15, 2) //free wheat seeds to start
      landOverlays.foreach(lOver => {
        if (lOver.purpose == wheatField) {
          //afterwards we could add more complex attributes for productivity
          val area: Double = lOver.getSurface
          val nWorker = math.round((area / CONSTANTS.HA_PER_WORKER).toFloat)
          val worker = if (nWorker > 0) nWorker else 1
          CONSTANTS.workercounter += worker
          val prodSpec: ProductionLineSpec = ProductionLineSpec(
            worker,
            List(/** (
                WheatSeeds,
                (area * CONSTANTS.WHEAT_SEEDS_PER_HA).toInt)*/),
            List(
              (WheatSeeds, (area * CONSTANTS.WHEAT_SEEDS_PER_HA).toInt),
              //(Fertilizer, 1)
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
        }
        //if some land overlays have paddoc purpose, add some cows inside
        //else if (lOver.purpose == paddoc){
        //  val nCows = 10 + scala.util.Random.nextInt(30)
        //  val prodSpec = new ProductionLineSpec(
        //    1, 
        //    List(),
        //    List((FeedStuff, nCows)),
        //    (),
        //    (Fertilizer, nCows),
        //    1 //TODO later add "List[(Commodity, Int)] instead of tuple in prod line spec 
        //  )
        //  hr.hire(1)
        //  crops ::= new CropProductionLine(lOver, prodSpec, this, hr.salary, s.timer)
        //  s.market(prodSpec.produced._1).add_seller(this)
        //}
      })
    }

    /** Returns whether everything was successfully bought. */
    protected def bulk_buy_missing(
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
        
        val alreadyBuyFrom = contactNetwork.contacts.map(_._1).toList
        //println("Already buy to")
        s.market(line._1).market_buy_order_now(s.timer, this, line._2,alreadyBuyFrom) == 0
      }
      l.forall(successfully_bought)
    }

    override def run_until(until: Int): Option[Int] = {
      // this ordering is important, so that bulk buying
      // happens before consumption.
      val nxt1 = super.run_until(until).get
      if (crops.isEmpty){
        Some(nxt1)
      }
      else {
        val nxt2 = crops.map(_.run_until(until).get).min
        Some(math.min(nxt1, nxt2)) // compute a meaningful next time
      }
      
    }

    /** The commodities asks may not be available immediatly
     * order is pass to the coop, which sells them back to this when products are buy by coop */ 
    def buyMissingFromCoop(toBuy: List[(Commodity, Int)]): Unit = {
      cooperative match {
        case None => println("Farm: " + this + "should be part of a cooperative to buy from it")
        case Some(coop) => {
          toBuy.foreach{
            case(com: Commodity, unit: Int) => (
              //Change this line 
              coop.buyLogs.update(this, coop.buyLogs(this) :+ (com, math.max(0, unit - available(com))))
            )
          }
        }
      }
    }

    def sellFromCoop(toSell: List[(Commodity, Int)]): Unit = {
      cooperative match {
        case None => println("Farm: " + this + "should be part of a cooperative to sell from it")
        case Some(coop) => {
          toSell.foreach{
            case(com: Commodity, unit: Int) => {
              assert(coop.saleableCommodities.contains(com))
              coop.sellLogs.update(this, coop.sellLogs(this) :+ (com, unit))
              sell_to(s.timer, coop, com, unit)
            }
          }
        }
      }
    }

    /** Chose for a given commodity between selling to cooperative or on its own to make the maximum money 
     * @return true if selling with coop is worth, else false */
    def sellToCoopWorth(com: Commodity): Boolean = {
      //For the moment, as coop sells in gross, price are a bit lower than the ones on the market.
      //Worthness of selling to coop is getting money instantly + sure to sell all at an okay price
      def getCoopPrice: Double = {
        0.98*s.prices.getPriceOf(com)
      }

      /** 1st Milestone: estimate profits/loss randomly (between 90 and 110 %)
       * 2nd Milestone: estimate profits/loss based on an expected value of how many and at which price stock is sell on market
       * A bit like someone who would speculate 
       * //TODO see if can add some contracts to ensure selling price ? 
       * */
      def getSelfPrice: Double = {
        (90.0 + scala.util.Random.nextInt(20))/100 * s.prices.getPriceOf(com)
      }
      
      cooperative match {
        case None => false // Will sell by itself, see if some stocks needs to be hold
        case Some(value) => getCoopPrice > getSelfPrice
        
      }
    }

    //TODO maybe should be declared inside seller or owner ? 
    /**
      * Implement a Fo Moo strategy (copy the others). If price is bearing(falling), sell. Else hold
      * If one turn remains before the holded commodities expires, sell all 
      */
    def sellingStrategy: Unit = {
      holdedCommodities.foreach{
              case (com: Security, (units:Int, expireTimer:Int)) => {
                val commodity = com.asInstanceOf[Commodity]
                if(s.timer < expireTimer - 1){
                  //Check if price has fallen
                  if(s.prices.getPriceOf(commodity) <= prevPrices.getOrElse(commodity,0.0)){
                    var toSell: Int = toSellEachTurn.getOrElse(commodity,0)/10
                    //avoid selling less than 10% of the stock each turn
                    if(holdedCommodity(com) - toSell < toSell){
                      //Sell all remaining stock
                      toSell = holdedCommodity(com)
                    }
                    val quantityToSell = Math.min(holdedCommodity(com), toSell)
                    if(sellToCoopWorth(commodity)){
                      sellFromCoop(List((commodity, quantityToSell)))
                    }
                    else{
                      releaseToMarket(commodity, quantityToSell)
                    }
                  }
                }
                //One turn remains before beeing expired, sell all of this commodity
                else if (s.timer == expireTimer - 1){
                  //Clear the hold inventory
                  //holdedCommodities.put(com, (0,0))

                  //TODO PUT THIS INSIDE A METHOD ??
                  if(sellToCoopWorth(commodity)){
                    sellFromCoop(List((commodity, units)))
                  }
                  else{
                    releaseToMarket(commodity, units)
                  }
                  //END TODO
                  //Otherwise, as removed from holded, could now be buy by everyone
                  toSellEachTurn.put(commodity, 0)
                }
                //update prevPrice of commodity
                prevPrices.put(commodity, s.prices.getPriceOf(commodity))
            }
          }
    }
    

    def canEqual(a: Any) = a.isInstanceOf[Farm]

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

package cooperative {

  import Securities.Commodities._
  import Simulation._
  import farmpackage.Farm
  import code._
  import scala.collection.mutable.Map

  /**
    * Buy production of `members` and sells them to market
    * Buy product for the community (e.g fertilizer, tractor,...). These products must be selled to members of the community
    * (after cause need contract on selling price) Part of benefits should be reverse to members 
    *
    * @param _farms:List[farm] : The initial members of the cooperative
    */
  class AgriculturalCooperative(_farms: List[Farm], _saleableCommodities: List[Commodity], s: Simulation) extends SimO(s){

    var members: List[Farm] = _farms

    // Group all commodities, in order to have better price afterwards
    private var commoditiesToBuy = scala.collection.mutable.Map[Commodity, Int]()
    
    //Used as a buffer, to buy/sell all stuff together, and redestribute goods/money 
    /** For each member, the commodities and their quantity that coop needs to buy to them */
    var buyLogs = scala.collection.mutable.Map[Farm, List[(Commodity, Int)]]()
    /** For each member, the commodities and their quantity that are sold by coop
     * Usefull only if we do not pay immediately farmers when they give commodities to coop (not the case atm)*/ 
    var sellLogs = scala.collection.mutable.Map[Farm, List[(Commodity, Int)]]() 

    var saleableCommodities: List[Commodity] = _saleableCommodities

    //init
    members.foreach(addMember(_))
    saleableCommodities.foreach(com => {
      s.market(com).add_seller(this)
    })
    commoditiesToBuy.put(WheatSeeds, 0)
    commoditiesToBuy.put(Fertilizer, 0) //TODO faire mieux que hardcoder
    commoditiesToBuy.put(FeedStuff, 0)
    //end init

    def addMember(member: Farm): Unit = {
      member.cooperative = Some(this)
      sellLogs.put(member, List[(Commodity, Int)]())
      buyLogs.put(member, List[(Commodity, Int)]())
    }

    def removeMember(member:Farm): Unit = {
      assert(members.contains(member) && sellLogs.contains(member) && buyLogs.contains(member))
      sellLogs.remove(member)
      buyLogs.remove(member)
      members = members.filterNot(_ == member)
    }

    /** Take the new orders from buyLogs, and put them in commoditiesToBuy. Clear buyLogs*/ 
    def updateCommoditiesToBuy: Unit = {
      buyLogs.foreach(
        elem => elem._2.foreach{
          case(com: Commodity, unit: Int) => commoditiesToBuy.update(com, commoditiesToBuy(com) + unit)
        }
      )
    }

    /** Sell to farms products they ordered and clear values of buyLogs */
    //TODO atm I assume that everything was bought succesfully, so we can just sell back the ask quantity to each 
    //member of buyLog. After include memory so if buy wasn't possible, sell it after and not now (else operation on empty inventory)
    def sellBackToFarm: Unit = {
      println("The buy Logs is: " + buyLogs)
      buyLogs.foreach(
        elem => elem._2.foreach{
          //TODO the price should be the one they paid for (almost no benefits, should be fair)
          //case(com: Commodity, unit: Int) => sell_to(s.timer, elem._1, com, unit)
          case(com: Commodity, unit: Int) => {
            if(unit > 0) sell_to(s.timer, elem._1, com, unit)
            //else println("The supplies " + com + "in quantity " + unit + " couldn't be bought")
          }
        }
      )
      buyLogs.keys.foreach(farm => buyLogs.put(farm, List[(Commodity, Int)]()))
      commoditiesToBuy.keys.foreach(com => commoditiesToBuy.update(com, 0))
    }

    override def mycopy(
        _shared: Simulation,
        _substitution: scala.collection.mutable.Map[SimO, SimO]
    ): SimO = ???

    //The price of a dummy is the average of the price of each farm 
    override def price(dummy: Commodity): Option[Double] = {
      if (available(dummy) > 0)
        Some(1.0 * inventory_avg_cost.getOrElse(dummy, 0.0))
      else None
    }


    protected def bulkBuyMissing(_l: List[(Commodity, Int)]): Boolean = {
      val l = _l.map(t => {
        // DANGER: if we have shorted his position, this amount is
        // not sufficient.
        val amount = math.max(0, t._2 - available(t._1))
        (t._1, amount)
      })

      def successfullyBought(line: (Commodity, Int)) = {
        
        val alreadyBuyFrom = contactNetwork.contacts.map(_._1).toList
        //println("Already buy to")
        s.market(line._1).market_buy_order_now(s.timer, this, line._2, alreadyBuyFrom) == 0
      }
      // nothing missing
      l.forall(successfullyBought)
    }

    //Each turn, check if some commodities need to be purchased
    override def algo: __forever = __forever(
      __do{
        updateCommoditiesToBuy
        //Now that things have been bought, we can sell them back to each farm that ask for
        bulkBuyMissing(commoditiesToBuy.toList) //TODO check condition if some buy couldn't be made
        sellBackToFarm
      },
      __wait(1),
    )
  }
}


//Correct the fact that we are selling 0 units of fertilizer(see terminal)