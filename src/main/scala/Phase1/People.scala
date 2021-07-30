package people

import Agents._
import code._
import places._
import geographic._

class People(_location: (String,String,String))extends Sim with Places{

  var location : (String,String,String) = _location
  val connectedTo: Road = ???
  var owner: Sim = this

  
  def algo: Instruction = ???

}