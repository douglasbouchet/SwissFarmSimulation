package enum {


  object TypeLand extends Enumeration{
    type TypeLand = Value
    
    val Cereals       = Value("wheat")
    val Paturage      = Value("flour")
    val Unknown       = Value("unknown")
  
    val allLandTypes = List(Cereals, Paturage, Unknown) 
  }

  object Goods extends Enumeration{
    type Goods = Value
    
    val Wheat       = Value("wheat")
    val Beef        = Value("beef")
    val WheatSeeds  = Value("wheatSeeds")
    val FeedStuff   = Value("feedStuff")
  
    val allGoods = List(Wheat, Beef, WheatSeeds, FeedStuff) 
  }

}


//add type of land etc....