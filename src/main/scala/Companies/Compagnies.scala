/**
 * This class is used in a first try to represent all the classes inheriting from Company (i.e the instanciable ones)
 * @note thoses make act basically the same each other, but we will add modification afterwards, like producing multiple commodities and so on...
 */

package Companies

import Simulation.{Simulation}
import _root_.Simulation.Factory.ProductionLineSpec
import geography.{CadastralParcel, LandAdministrator}
import Simulation.Factory.Factory
import Simulation.SimO
import scala.collection.mutable

//This class will take code from Buyer, but need to modify some of them (e.g selling multiples commodities). That's why we don't extend
//class Supermarket(s: Simulation,landAdmin: LandAdministrator, _parcels: List[CadastralParcel], _comToBuy: mutable.Map[Commodity, Double]) extends SimO(s){
case class Supermarket(s: Simulation,lAdmin: LandAdministrator, _parcels: List[CadastralParcel], pls: ProductionLineSpec) 
                      extends Company(s, lAdmin, _parcels, List(new Factory(pls, s))){

  //val comToBuy: mutable.Map[Commodity, Double] = _comToBuy
  lAdmin.addAgent(this)


}

case class Mill(s: Simulation, lAdmin: LandAdministrator, _parcels: List[CadastralParcel], pls: ProductionLineSpec) 
                extends Company(s, lAdmin, _parcels, List(new Factory(pls, s))){
  lAdmin.addAgent(this)
  
}
