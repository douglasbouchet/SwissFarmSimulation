import org.scalatest.funsuite
import org.scalatest._

class MySuite extends funsuite.AnyFunSuite {

    test("hello") {
        val obtained = 42
        val expected = 43
        assert(obtained === expected)
    }
}


class mySuite2 extends flatspec.AnyFlatSpec {
    "The simple trading example" should "work" in {
        val obtained = 43
        val expected = 43
        assert(obtained === expected)
    }
}