package Agents{

  
  object cowState extends Enumeration{
    type CowState = Value
    
    val pregnant    = Value("Pregnant")
    val rdyToBeEat  = Value("RdyToBeEat")
    val calf        = Value("Calf")
    val normal      = Value("Normal")
    }
}