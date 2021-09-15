package farmrelated.cooperative{

  import Securities.Commodities._
  import Simulation._
  import farmpackage.Farm
  import code._

  import scala.collection.mutable

  //import economicsimulations._

  /**
    * Buy production of `members` and sells them to market
    * Buy product for the community (e.g fertilizer, tractor,...). These products must be sold to members of the community
    * (after cause need contract on selling price) Part of benefits should be reverse to members 
    *
    * @param _farms:List[farm] : The initial members of the cooperative
    */
  class AgriculturalCooperative(_farms: List[Farm], _saleableCommodities: List[Commodity], s: Simulation) extends SimO(s){

    var members: List[Farm] = _farms

    // Group all commodities, in order to have better price afterwards
    private val commoditiesToBuy = scala.collection.mutable.Map[Commodity, Int]()
    
    //Used as a buffer, to buy/sell all stuff together, and redistribute goods/money
    /** For each member, the commodities and their quantity that coop needs to buy to them */
    //var buyLogs = scala.collection.mutable.Map[Farm, List[(Commodity, Int)]]()
    var buyLogs: mutable.Map[Farm, mutable.Map[Commodity, Int]] = scala.collection.mutable.Map[Farm, mutable.Map[Commodity, Int]]()
    /** For each member, the commodities and their quantity that are sold by coop
     * Useful only if we do not pay immediately farmers when they give commodities to coop (not the case atm)*/
    val sellLogs: mutable.Map[Farm, List[(Commodity, Int)]] = scala.collection.mutable.Map[Farm, List[(Commodity, Int)]]()

    var saleableCommodities: List[Commodity] = _saleableCommodities

    //Used to arrange crop type distribution among members:
    val membersCropsType: mutable.Map[Farm, mutable.Map[Commodity, Int]] = mutable.Map[Farm, mutable.Map[Commodity, Int]]()

    //init
    members.foreach(addMember)
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
      buyLogs.put(member, mutable.Map[Commodity, Int]())
    }

    def removeMember(member:Farm): Unit = {
      assert(members.contains(member) && sellLogs.contains(member) && buyLogs.contains(member))
      sellLogs.remove(member)
      buyLogs.remove(member)
      members = members.filterNot(_ == member)
    }

    /** Take the new orders from buyLogs, and put them in commoditiesToBuy. Clear buyLogs*/ 
    def updateCommoditiesToBuy(): Unit = {
      buyLogs.foreach(
        elem => elem._2.foreach{
          case(com: Commodity, unit: Int) => commoditiesToBuy.update(com, commoditiesToBuy(com) + unit)
        }
      )
    }

    /** Sell to farms products they ordered and clear values of buyLogs */
    //TODO atm I assume that everything was bought successfully, so we can just sell back the ask quantity to each
    //member of buyLog. After include memory so if buy wasn't possible, sell it after and not now (else operation on empty inventory)
    def sellBackToFarm(): Unit = {
      buyLogs.foreach(
        elem => elem._2.foreach{
          //TODO the price should be the one they paid for (almost no benefits, should be fair)
          //case(com: Commodity, unit: Int) => sell_to(s.timer, elem._1, com, unit)
          case(com: Commodity, unit: Int) =>
            if(unit > 0) sell_to(s.timer, elem._1, com, unit)
            //else println("The supplies " + com + "in quantity " + unit + " couldn't be bought")
        }
      )
      buyLogs.keys.foreach(farm => buyLogs.put(farm, mutable.Map[Commodity, Int]()))
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
        
        val alreadyBuyFrom = contactNetwork.contactsSellingCom(line._1)
        //val alreadyBuyFrom = contactNetwork.contacts.map(_._1).toList
        //println("Already buy to")
        s.market(line._1).market_buy_order_now(s.timer, this, line._2, alreadyBuyFrom) == 0
      }
      // nothing missing
      l.forall(successfullyBought)
    }

    //should be called by the farmer, in order to see what crops are planned by other members 
    def getNextCrops(): Unit = {} //TODO

    //Each turn, check if some commodities need to be purchased
    override def algo: __forever = __forever(
      __do{
        updateCommoditiesToBuy()
        //Now that things have been bought, we can sell them back to each farm that ask for
        bulkBuyMissing(commoditiesToBuy.toList) //TODO check condition if some buy couldn't be made
        sellBackToFarm()
      },
      __wait(1)
    )
  }
}