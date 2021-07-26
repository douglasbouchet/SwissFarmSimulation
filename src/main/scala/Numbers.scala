package Agents{

    //Math
    import scala.util.Random

    object Numbers {
    
    val PREGNANCY_DURATION = 9

    def randomBetween(start: Int, end: Int): Int = {
        val r = new scala.util.Random
        start + r.nextInt((end - start) + 1)  
    }
    }
}