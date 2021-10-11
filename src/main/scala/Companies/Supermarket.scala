package Companies

import Securities.Commodities.Commodity
import Simulation.{SimO, Simulation}
import code.Instruction
import geography.{CadastralParcel, LandAdministrator}

import scala.collection.mutable

//This class will take code from Buyer, but need to modify some of them (e.g selling multiples commodities). That's why we don't extend
class Supermarket(s: Simulation,landAdmin: LandAdministrator, _parcels: List[CadastralParcel], _comToBuy: mutable.Map[Commodity, Double]) extends SimO(s){

  val parcels: List[CadastralParcel] = _parcels
  val comToBuy: mutable.Map[Commodity, Double] = _comToBuy

  require(!parcels.isEmpty)
  landAdmin.addAgent(this)


  override def mycopy(_shared: Simulation, _substitution: mutable.Map[SimO, SimO]): SimO = ???


  override protected def algo: Instruction = ???
}
