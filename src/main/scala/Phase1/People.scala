package people

import Agents._
import code._
import places._
import geographic._


//TODO see if people need to extend Trader in order to buy food 
//change the type of place in function of people e.g a farmer live in a farm, other people live in house. etc
class People(_place : Place)extends Sim{

  var place: Place = _place
  
  //Buy food (in supermarket, butcher etc...)
  def algo: Instruction = ???

}