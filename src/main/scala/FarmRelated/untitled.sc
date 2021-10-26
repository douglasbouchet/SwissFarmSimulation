import FarmRelated.Farmer
import Owner.Owner
//import Securities.Commodities.{Commodity, Flour, Wheat, all_commodities}
import Securities.Commodities._
import Simulation.Simulation
import geography.CadastralParcel
import modifyFromKoch.Person
import FarmRelated.Production

val s = new Simulation()
val owner = new Owner()
owner.make(WheatSeeds, 50, 1)
val nEmployee = 0
val salary = 2000
val consumed = List((WheatSeeds, 100))
val produced = List((Wheat, 100))
val timeToComplete = 10
val employees = scala.collection.mutable.Stack[Person]()
val parcel = new CadastralParcel(("doug", 1111), owner ,List(), 10)

val prod = new Production(s, owner, nEmployee, salary, consumed, produced, timeToComplete, employees)

print(owner.inventory_to_string())
for (i <- 1 to 20){
  println()
  print("s timer =" + s.timer)
  print("turn " + i + "get prod = " + prod.getProduction)
  s.timer += 1
}

print(owner.inventory_to_string())

