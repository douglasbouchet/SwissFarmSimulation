/*hors zone à batir
  On peut construire bâtiment pour l'agriculture: silos, engar, ferme. Voir cependant avec la lois
  + egalement stockage des productions agricoles, culture hors-sol
*/



//Used by mill, bakery, supermaket location
class Land() {

val location: (String,String,String,String) // (canton,district,city,adress)
val surface : Int //the surface in ha
val connectedTo: Road

val price : Int 

val owner: Owner 
  
}

/* 
Prohibited to have any installion not related to agriculture
*/
class OutBuildZoneLand extends Land(){
  
  //goal: rent truck between farmer etc..
  val togetherOperatedLands : List[Land] = ??? //could be stored in cooperatives instead

  val waterSources = ??? //Used to model the impact of crops into water. Type = ?
  
  val crops : List[Crop] //The different crops

  val assets : List[Assets] // e.g cereal bins, livestock, machinery

  //val polygonShape  
}

//
class Crop(){

  val fieldSurface : Int = ??? //used to compute the productivity of production line

  //Other attributs may be added depending on which pollution aspect we want to focus
  val nutrientsConcentration : Double = ???
  val nutrateConcentration : Double = ???

}




/*This a is a tree/Hashmap, used to find lands near to a given one ()
Leaf contains the tuple (complete address, ownerId, )
*/

object LandLocalizator {

}