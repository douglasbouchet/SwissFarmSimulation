package Government

import FarmRelated.Farmer
import Securities.Commodities.Commodity
import Simulation.{SimO, Simulation}
import code.{Instruction, __do}

import scala.collection.mutable

class Government(s: Simulation) extends SimO(s){

  private var policies = List[Policy]()

  private def addPolicy(policy: Policy): Unit = policies ::= policy
  private def removePolicy(policy: Policy): Unit = policies = policies.filterNot(_ == policy)
  def getPolicies: List[Policy] = policies

  private var farmers: List[Farmer] = List[Farmer]()
  def addFarmer(farmer: Farmer): Unit = farmers ::= farmer
  def removeFarmer(farmer: Farmer): Unit = farmers = farmers.filterNot(_ == farmer)


  //--------Variables and Methods for price evaluation------------------------------------------------------
  //this represent the annual consumption of population and exports
  var consumption: mutable.Map[Commodity, Int] = scala.collection.mutable.Map[Commodity, Int]()

  def demandFromPrice(p: Int): Int = 3*Math.pow(p,-2).toInt

  def supplyFromPrice(p: Int): Int = (0.5*Math.pow(p,0.5)).toInt

  //we can solve equation by trying several values for x, but we dont find the best solution (see with zilu
  // if we do this, how to do this.....)
  def findMin: Int = ???

  //-----------------------------------------------------------------------------------------


  override def mycopy(_shared: Simulation, _substitution: mutable.Map[SimO, SimO]): SimO = ???

  override protected def algo: Instruction = __do{
    addPolicy(MaximumPollution())
    addPolicy(TaxPollutingCrops())
    println("The policies are : " + getPolicies)
  }



}
