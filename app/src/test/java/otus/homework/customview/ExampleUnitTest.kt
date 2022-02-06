package otus.homework.customview

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.reflect.KClass

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    inline fun <reified T : Any> MutableMap<Int, T>.putInto(mutableMap: MutableMap<KClass<*>, MutableMap<Int, out Any>>): MutableMap<Int, T> {
        val class_ = T::class
        mutableMap[class_] = this
        return this
    }

    inline fun <reified T : Any> KClass<T>.createIntToTypeMap(): MutableMap<Int, T> {
        return mutableMapOf()
    }

    @Test
    fun creatingAdaptorInfrastructure() {
        val cells: List<Any> = listOf(
            InterpolatorDelegateCell(InterpolatorEnum.LINEAR,
                InterpolatorEnum.LINEAR.name),
            InterpolatorDelegateCell(InterpolatorEnum.LINEAR_OUT_SLOW_IN,
                InterpolatorEnum.LINEAR_OUT_SLOW_IN.name),
            InterpolatorDelegateCell(InterpolatorEnum.ACCELERATE,
                InterpolatorEnum.ACCELERATE.name),
            SomeOtherCell(1),
            SomeOtherCell(2),
            InterpolatorDelegateCell(InterpolatorEnum.ACCELERATE_DEC,
                InterpolatorEnum.ACCELERATE_DEC.name),
        )
        val maps: MutableMap<KClass<*>, MutableMap<Int, out Any>> = mutableMapOf()

        val interpolatorCells =
            InterpolatorDelegateCell::class.createIntToTypeMap().putInto(maps)
        val someOtherCells = SomeOtherCell::class.createIntToTypeMap().putInto(maps)

        // automatic code begin ------------------------

        for ((index, value) in cells.withIndex()) {
            val positionToCellsMap = maps[value::class] ?: continue
            @Suppress("UNCHECKED_CAST")
            value::class.castAndPutInto(index, value, positionToCellsMap as MutableMap<Int, Any>)
        }
        // automatic code end ------------------------

        // manual code
        val cellsViewTypes = cells.map {
            when (it) {
                is InterpolatorDelegateCell -> 0
                is SomeOtherCell -> 1
                else -> Int.MAX_VALUE
            }
        }

        println(interpolatorCells)
        println(cellsViewTypes)
    }
}
