package Agents{

  
  object cowState extends Enumeration{
    type CowState = Value
    
    val pregnant    = Value("pregnant")
    val rdyToBeEat  = Value("RdyToBeEat")
    val calf        = Value("Calf")
    val dead        = Value("Dead")
    }

  object Goods extends Enumeration{
    type Goods = Value
    
    val Wheat       = Value("wheat")
    val Flour       = Value("flour")
    //val Land        = Value("land")
    val Beef        = Value("beef")
    val Steak       = Value("steak")
    val WheatSeeds  = Value("wheatSeeds")
    val Bread       = Value("bread")
    val FeedStuff   = Value("feedStuff")
  
    val allGoods = List(Wheat, Beef, Steak, WheatSeeds, Bread, FeedStuff) 
  }
}