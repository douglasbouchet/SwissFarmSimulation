package farmpackage {

  import Simulation._
  import Simulation.Factory._
  import landAdministrator.CadastralParcel
  import landAdministrator.LandOverlay
  import landAdministrator.LandOverlayPurpose._
  import code._
  import Securities.Commodities._
  import scala.collection.mutable

  case class Farm(s: Simulation) extends SimO(s) {

    var parcels: List[CadastralParcel] = List()
    var landOverlays: List[LandOverlay] = List()
    var crops: List[CropProductionLine] = List[CropProductionLine]()

    //use afterwards to model other co2 emission
    var Co2: Double = 0

    protected var hr: HR = new HR(s, this)

    def addParcels(newParcels: List[CadastralParcel]) {
      parcels :::= newParcels
    }

    override def price(dummy: Commodity): Option[Double] = {
      if (crops.length > 0 && (available(dummy) > 0))
        Some(1.0 * inventory_avg_cost.getOrElse(dummy, 0.0))
      else None
    }

    //private def changeActivity(newState: Boolean, prodL: ProductionLine) {prodL.active = newState}

    override def stat = {
      //println(s"$name \n " + inventory_to_string())
      //println(s"$name \n")
    }
    override def algo = __forever(
      __do {
        crops.foreach(crop => bulk_buy_missing(crop.pls.consumed, 1))
        s.observator.Co2 += crops.map(_.Co2Emitted).sum
        crops.foreach(crop => {crop.Co2Emitted = 0.0})
        //crops.foreach(crop => changeActivity(false, crop))
      },
      __wait(1),
      __do {
        assert(hr.employees.length == crops.map(_.pls.employees_needed).sum)
        hr.pay_workers();
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
    def init = {
      landOverlays.foreach(lOver => {
        if (lOver.purpose == wheatField) {
          //afterwards we could add more complex attributs for productivity
          val area: Double = lOver.getSurface
          var nWorker = math.round((area / CONSTANTS.HA_PER_WORKER).toFloat)
          val worker = if (nWorker > 0) nWorker else 1
          CONSTANTS.workercounter += worker
          val prodSpec = new ProductionLineSpec(
            worker,
            List((
                WheatSeeds,
                math.round((area * CONSTANTS.WHEAT_SEEDS_PER_HA).toFloat))
            ),
            List(( WheatSeeds, math.round((area * CONSTANTS.WHEAT_SEEDS_PER_HA).toFloat))),
            (
              Wheat,
              math.round((area * CONSTANTS.WHEAT_PRODUCED_PER_HA).toFloat)
            ),
            6
          )
          hr.hire(prodSpec.employees_needed)
          val prodL = new CropProductionLine(lOver, prodSpec, this, hr.salary, s.timer)
          crops ::= prodL
          s.market(prodSpec.produced._1).add_seller(this);
        }
      })
      //s.sims :::= crops
    }

    /** Returns whether everything was sucessfully bought. */
    protected def bulk_buy_missing(
        _l: List[(Commodity, Int)],
        multiplier: Int
    ): Boolean = {
      val l = _l.map(t => {
        // DANGER: if we have shorted his position, this amount is
        // not sufficient.
        val amount = math.max(0, t._2 * multiplier - available(t._1));
        (t._1, amount)
      });

      def successfully_bought(line: (Commodity, Int)) =
        (s.market(line._1).market_buy_order_now(s.timer, this, line._2) == 0);

      // nothing missing

      l.forall(successfully_bought)
    }

    override def run_until(until: Int): Option[Int] = {
      // this ordering is important, so that bulk buying
      // happens before consumption.
      val nxt1 = super.run_until(until).get;
      val nxt2 = crops.map(_.run_until(until).get).min;
      Some(math.min(nxt1, nxt2)) // compute a meaningful next time
    }
  }

}
