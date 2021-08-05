package Agents 

import Trader._
import code.Instruction
import network.Road
import people._
import places._
import owner._
import enum.Goods._

trait Compagnies extends Sim with Trader{

  //Used to represent the stock of the compagnies 
  var owner: Owner 
  //This is where the compagny is
  var place : Place 
  
  var required: List[Goods] = List()
  var employee : List[People]

  def hire(): Boolean
  def fire(): Boolean


  //Boolean in phase 1 cause on/off for production, in phase + change type
  //Use the inventory of the owner to check, + may need access to market from trader 
  //in order to perform actions to fullfiull the stuff required
  def fullfillRequiered(required: List[Goods]): Boolean = ???

  //remains to see if we still use a factory or do we use a personnal type that is defined only here
  //replace all the factory in a simpler sens for the moment, but may should be more complex in phase + 
  def produce(required: List[Goods], produced: List[Goods]) = ???

}


//TODO change by Farm, currently used in upper directory
case class Farm(_capital : Int, _connectedTo : Road, _location: (String,String,String), _surface: Int) extends Compagnies {

  //Herited methods/objects
  override var employee: List[People] = _
  override var place : Place = new Land(_connectedTo, _location, _surface, this)

  override def hire(): Boolean = ???
  override def fire(): Boolean = ???

  //find the supplies need for the cows, the agriculture, and sell them
  override protected def algo: Instruction = ???
  var owner: Owner = new Owner(_capital)

  //Added methods + attributs (TODO complete)
  var herd: List[Animals] = List()
}

case class BasicSupplier(){
}