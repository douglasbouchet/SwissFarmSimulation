package factory

  import code._
  import Agents._
  import `enum`.Goods.Goods
  import owner._
  trait Factory{

    case class ProductionLineSpec(employees_needed: Int,
                                  required: List[(Goods, Int)],
                                  consumed: List[(Goods, Int)],
                                  produced: (Goods, Int),
                                  time_to_complete: Int) {

      def theoretical_max_productivity(): Double =
        produced._2.toDouble / time_to_complete
    }



    // does not do its own buying
    case class ProductionLine(
       val pls: ProductionLineSpec,
       var o: Owner,
       val salary: Int,
       val start_time: Int,
       //  var log : List[(Int, Double)] = List(),
       // (time production run was completed, efficiency of prod
       // state of the machine (this plus env)
       var goodwill : Double = 0.0,
       var lost_runs_cost : Double = 0.0,
       // cost from zero-efficiency production runs
       private var rpt : Int = 0,
       private var frac : Double = 1.0,
       // fraction of theoretical capacity currently achieved
       private var costs_consumables : Double = 0.0 // of current production run
      ) extends Sim {
      init(start_time);

      def make()

      protected def algo = __forever(
        __do { // start of production run
          costs_consumables = 0;
          //print("buying consumables: " + o + " " + this + ". ");
          frac = 1.0;
          for(x <- pls.consumed) {
            val n = math.min(o.available(x._1), x._2); // requested and available
            costs_consumables += o.destroy(x._1, n);
            frac = math.min(frac, n.toDouble / x._2);
          }
          goodwill = costs_consumables;
          if(frac < 1.0)
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
          val units_produced = (pls.produced._2  * frac).toInt;
          val personnel_costs = pls.employees_needed * salary *
            pls.time_to_complete;
          val total_cost : Double = costs_consumables + personnel_costs;
          val unit_cost = total_cost / units_produced;

          if(units_produced > 0) {
            o.make(pls.produced._1, units_produced, unit_cost);
          }
          else {
            lost_runs_cost += total_cost;
          }
          //      log = (get_time, frac) :: log;
        }
      )
    }



    // TODO: different capabilities and salaries per employee
    case class HR(private val shared: Simulation,
                  private val o: Owner,
                  salary: Int = 20000 * 100, // 20kEUR
                  employees: collection.mutable.Stack[Person] =
                  collection.mutable.Stack[Person]()
                 ) {
      def pay_workers() { for(a <- employees) o.transfer_money_to(a, salary); }
      def salary_cost() = salary * employees.length

      protected def hire_one() {
        if(shared.labour_market.length > 0)
          employees.push(shared.labour_market.pop.asInstanceOf[Person]);
      }
      protected def fire_one() { shared.labour_market.push(employees.pop); }

      def hire(n: Int) { for(i <- 1 to n) hire_one(); }
      def fire(n: Int) { for(i <- 1 to n) fire_one(); }
    }


    class Factory(pls: ProductionLineSpec) extends SimO(shared) {
      var pl : List[ProductionLine] = List()
      var prev_mgmt_action : Int = 0
      protected var hr : HR = new HR(shared, this)
      protected var goal_num_pl = 0;

      // constructor
      {
        goal_num_pl = 1; // have one production line
      }

      protected def copy_state_to(_to: Factory) { assert(false); } // don't call
      protected def copy_state_to(_to: Factory,
                                  _shared: Simulation,
                                  old2new: collection.mutable.Map[SimO, SimO]) {

        //println("Factory.copy_state_to: " + this);
        super.copy_state_to(_to);

        _to.pl = pl.map(_.mycopy(_to));
        _to.zombie_cost2 = zombie_cost2;
        _to.prev_mgmt_action = prev_mgmt_action;
        _to.hr = new HR(_shared, _to, hr.salary,
          hr.employees.map(old2new(_).asInstanceOf[Person])
        );
        _to.goal_num_pl = goal_num_pl;
      }
      def mycopy(_shared: Simulation,
                 _substitution: collection.mutable.Map[SimO, SimO]) = {
        val f = new Factory(pls, _shared);
        copy_state_to(f, _shared, _substitution);
        f
      }


      /** Returns whether everything was sucessfully bought. */
      protected def bulk_buy_missing(_l: List[(Commodity, Int)],
                                     multiplier: Int) : Boolean = {
        val l = _l.map(t => {
          // DANGER: if we have shorted his position, this amount is
          // not sufficient.
          val amount = math.max(0, t._2 * multiplier - available(t._1));
          (t._1, amount)
        });

        def successfully_bought(line: (Commodity, Int)) =
          (shared.market(line._1).
            market_buy_order_now(shared.timer, this, line._2) == 0);
        // nothing missing

        l.forall(successfully_bought)
      }


      protected def add_production_line() : Boolean = {
        var success = true;

        if(shared.labour_market.length >= pls.employees_needed)
        {
          // buy only what we require. We may still have it from
          // previous production reductions.
          success = bulk_buy_missing(pls.required, pl.length + 1);
          if(success) {
            hr.hire(pls.employees_needed);
            pl = new ProductionLine(pls, this, hr.salary, shared.timer) :: pl;
            //pl.head.init(shared.timer);
          }
        }
        else success = false;
        success
      }

      // We don't sell required items (land, etc.) but only fire people.
      protected def remove_production_line() {
        if(pl.length > 0) {
          hr.fire(pls.employees_needed);
          zombie_cost2 += pl.head.goodwill;
          pl = pl.tail;
        }
      }

      protected def goodwill: Int = pl.map(_.goodwill).sum.toInt

      override def stat {
      }


      override protected def algo = __forever(
        __do {
          val mgmt_step_size = 6;

          if(prev_mgmt_action + mgmt_step_size < shared.timer)
          //if(shared.timer % mgmt_step_size == mgmt_step_size - 1)
          {
            prev_mgmt_action = shared.timer; // call before tactics to avoid
            // immediate recursion in nested simulation.
          }

          for(i <- (pl.length + 1) to goal_num_pl)
            add_production_line();
          for(i <- (goal_num_pl + 1) to pl.length)
            remove_production_line();

          // TODO: buy more to get better prices?
          //println("Factory.algo: this=" + this);
          val still_missing = bulk_buy_missing(pls.consumed, pl.length);
        },
        __wait(1),
        __do {
          assert(hr.employees.length == pl.length * pls.employees_needed);
          hr.pay_workers();
        }
      )

      override def run_until(until: Int) : Option[Int] = {
        // this ordering is important, so that bulk buying
        // happens before consumption.
        val nxt1 = super.run_until(until).get;
        val nxt2 = pl.map(_.run_until(until).get).min;
        Some(math.min(nxt1, nxt2)) // compute a meaningful next time
      }
    }
    
    
  }
