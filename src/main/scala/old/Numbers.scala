package Agents{

    //Math
    import scala.util.Random

    object Numbers {
    
    val PREGNANCY_DURATION = 9

    val KG_CO2_PER_KG_MEAT_GRASSLAND : Double = 0.371
    val KG_CO2_PER_KG_MEAT_NOT_GRASSLAND : Double = 0.398

    def randomBetween(start: Int, end: Int): Int = {
        val r = new scala.util.Random
        start + r.nextInt((end - start) + 1)  
    }
    }
}