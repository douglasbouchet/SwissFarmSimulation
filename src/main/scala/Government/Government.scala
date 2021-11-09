package Government

import FarmRelated.Farmer
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


  override def mycopy(_shared: Simulation, _substitution: mutable.Map[SimO, SimO]): SimO = ???

  override protected def algo: Instruction = __do{
    addPolicy(MaximumPollution())
    addPolicy(TaxPollutingCrops())
    println("The policies are : " + getPolicies)
  }



}
