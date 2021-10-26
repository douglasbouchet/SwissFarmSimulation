import org.scalatest.funsuite
import org.scalatest._
import FarmRelated.Production
import Owner.Owner
import Securities.Commodities._
import Simulation.Simulation
import geography.CadastralParcel
import modifyFromKoch.Person

class ProductionTest extends funsuite.AnyFunSuite {

  val s = new Simulation()
  val owner = new Owner()

  val nEmployee = 0
  val salary = 2000
  val consumed = List((WheatSeeds, 100))
  val produced = List((Wheat, 100))
  val timeToComplete = 10
  val employees = scala.collection.mutable.Stack[Person]()


  test("Frac and Produced Quantites are correct"){
    val s = new Simulation()
    val poorOwner = new Owner()
    poorOwner.make(WheatSeeds, 50, 1)
    print(poorOwner.inventory_to_string())
    val prod0 = new Production(s, poorOwner, nEmployee, salary, consumed, produced, timeToComplete, employees)
    assert(prod0.frac === 0.5)
    s.timer += timeToComplete
    prod0.getProduction
    assert(poorOwner.available(Wheat) === 50)

    val goodOwner = new Owner()
    goodOwner.make(WheatSeeds, 100, 1)
    val prod1 = new Production(s, goodOwner, nEmployee, salary, consumed, produced, timeToComplete, employees)
    assert(prod1.frac === 1.0)
    s.timer += timeToComplete
    prod1.getProduction
    assert(goodOwner.available(Wheat) === 100)

    val prodNull = new Production(s, poorOwner, nEmployee, salary, consumed, produced, timeToComplete, employees)
    assert(prodNull.frac === 0)
    s.timer += timeToComplete
    assert(poorOwner.available(Wheat) === 50)
  }

}







