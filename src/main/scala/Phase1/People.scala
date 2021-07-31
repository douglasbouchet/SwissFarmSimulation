package people

import Agents._
import code._
import places._
import geographic._


//TODO see if people need to extend Trader in order to buy food 
class People(_location: (String,String,String))extends Sim with Places{

  var location : (String,String,String) = _location
  val connectedTo: Road = ???
  var owner: Sim = this

  
  //Buy food (in supermarket, butcher etc...)
  def algo: Instruction = ???

}