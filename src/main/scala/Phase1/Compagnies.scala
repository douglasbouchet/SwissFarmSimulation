package Agents 

import Trader._
import code.Instruction
import factory._
import geographic.Road
import people._
import places._

trait Compagnies extends Sim with Trader with Factory with Places{

  var employee : List[People]
  //Used to buy stuff and pay employee
  var budget   : Int
  //This is where the compagny is
  var land     : Land 


  def hire(): Boolean
  def fire(): Boolean

}


//TODO change by Farm, currently used in upper directory
case class Farmer(_balance : Balance, _connectedTo : Road, _location: (String,String,String)) extends Compagnies {

  //Herited methods/objects
  override var employee: List[People] = _
  override var budget: Int = _
  override var land: Land = _

  override def hire(): Boolean = ???

  override def fire(): Boolean = ???

  //find the supplies need for the cows, the agriculture, and sell them
  override protected def algo: Instruction = ???

  override var balance: Balance = _balance
  override val connectedTo: Road = _connectedTo
  override var location: (String, String, String) = _location
  override var owner: Sim = this

  //Added methods + attributs (TODO complete)
  var herd: List[Animals]


}