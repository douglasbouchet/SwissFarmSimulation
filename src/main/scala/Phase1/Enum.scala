package enum {


  object TypeLand extends Enumeration{
    type TypeLand = Value
    
    val Cereals       = Value("cereals")
    val Paturage      = Value("paturage")
    val Unknown       = Value("unknown")
  
    val allLandTypes = List(Cereals, Paturage, Unknown) 
  }

  object Goods extends Enumeration{
    type Goods = Value
    
    val Cereals     = Value("cereals")
    val Beef        = Value("beef")
    val WheatSeeds  = Value("wheatSeeds")
    val FeedStuff   = Value("feedStuff")
  
    val allGoods = List(Cereals, Beef, WheatSeeds, FeedStuff) 
  }

}
