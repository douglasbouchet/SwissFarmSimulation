package Government

import Simulation.{SimO, Simulation}
import code.{Instruction, __do}

import scala.collection.mutable

class Government(s: Simulation) extends SimO(s){

  private var policies = List[Policy]()

  private def addPolicy(policy: Policy) = policies ::= policy

  private def removePolicy(policy: Policy) = policies = policies.filterNot(_ == policy)

  def getPolicies: List[Policy] = policies


  override def mycopy(_shared: Simulation, _substitution: mutable.Map[SimO, SimO]): SimO = ???

  override protected def algo: Instruction = __do{
    addPolicy(ConvertToOrganic())
    println("The policies are : " + getPolicies)
  }
}
