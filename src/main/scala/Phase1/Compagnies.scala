package Agents 

import Trader._
import factory._
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