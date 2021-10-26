import org.scalatest.funsuite
import FarmRelated.Production
import Owner.Owner
import Securities.Commodities._
import Simulation.{SimO, Simulation}
import geography.{CadastralParcel, LandOverlay, LandOverlayPurpose}
import modifyFromKoch.Person

import scala.collection.mutable

class ProductionTest extends funsuite.AnyFunSuite {

  val s = new Simulation()
  val owner = new Owner()

  val nEmployee = 2
  val salary = 2000
  val consumed = List((WheatSeeds, 100))
  val produced = List((Wheat, 100))
  val timeToComplete = 10
  val employees: mutable.Stack[SimO] = scala.collection.mutable.Stack[SimO]()
  val parcel = new CadastralParcel(("doug city", 1111), owner, List(), 1)
  val lOver = new LandOverlay(List((parcel, 1.0)))
  val persons: List[SimO] = (for (_ <- 1 to 10) yield new Person(s, false, parcel).asInstanceOf[SimO]).toList
  employees.pushAll(persons)
  s.labour_market = employees

  test("Frac and Produced Quantities and number hired people are correct"){
    val poorOwner = new Owner()
    poorOwner.make(WheatSeeds, 50, 1)
    print(poorOwner.inventory_to_string())
    val prod0 = new Production(s, poorOwner, nEmployee, salary, consumed, produced, timeToComplete)
    assert(prod0.frac === 0.5)
    assert(s.labour_market.length == 10 - nEmployee)
    s.timer += timeToComplete
    prod0.getProduction
    assert(poorOwner.available(Wheat) === 50)
    assert(s.labour_market.length == 10)

    val goodOwner = new Owner()
    goodOwner.make(WheatSeeds, 100, 1)
    val prod1 = new Production(s, goodOwner, nEmployee, salary, consumed, produced, timeToComplete)
    assert(prod1.frac === 1.0)
    s.timer += timeToComplete
    prod1.getProduction
    assert(goodOwner.available(Wheat) === 100)

    employees.clear()

    val prodNull = new Production(s, poorOwner, nEmployee, salary, consumed, produced, timeToComplete)
    assert(prodNull.frac === 0)
    s.timer += timeToComplete
    assert(poorOwner.available(Wheat) === 50)
  }

  test("LandOverlayPurpose correctly reset to noPurpose after die"){
    lOver.purpose = LandOverlayPurpose.wheatField
    val prod = new Production(s, new Owner(), nEmployee, salary, consumed, produced, timeToComplete, landOverlay=Some(lOver))
    assert(lOver.purpose == LandOverlayPurpose.wheatField)
    prod.die()
    assert(lOver.purpose == LandOverlayPurpose.noPurpose)

  }

}







