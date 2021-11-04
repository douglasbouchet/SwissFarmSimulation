package FarmRelated
import Simulation._
import code.Instruction

import scala.collection.mutable

//class Child(s: Simulation, _age: Int, _gender: String, _want_take_over: Boolean) extends SimO(s) {
class Child(s: Simulation, _age: Int, _gender: String, _want_take_over: Boolean){

    val want_take_over: Boolean = _want_take_over
    val gender = _gender
    val age = _age

    //override def mycopy(_shared: Simulation, _substitution: mutable.Map[SimO, SimO]): SimO = ???

    //override protected def algo: Instruction = __forever()

}
