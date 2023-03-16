package otus.homework.customview

import org.junit.Test

import org.junit.Assert.*
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val deltaX = -3f
        val deltaY = 5f
        val distance = sqrt(deltaX.pow(2) + deltaY.pow(2))
        val angle = atan(deltaY/deltaX)
        val angleDegree = angle * 180f / PI
        println(angleDegree)
        assertEquals(4, 2 + 2)
    }
}