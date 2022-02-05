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
            MainActivity.InterpolatorDelegateCell(MainActivity.InterpolatorEnum.LINEAR,
                MainActivity.InterpolatorEnum.LINEAR.name),
            MainActivity.InterpolatorDelegateCell(MainActivity.InterpolatorEnum.LINEAR_OUT_SLOW_IN,
                MainActivity.InterpolatorEnum.LINEAR_OUT_SLOW_IN.name),
            MainActivity.InterpolatorDelegateCell(MainActivity.InterpolatorEnum.ACCELERATE,
                MainActivity.InterpolatorEnum.ACCELERATE.name),
            MainActivity.SomeOtherCell(1),
            MainActivity.SomeOtherCell(2),
            MainActivity.InterpolatorDelegateCell(MainActivity.InterpolatorEnum.ACCELERATE_DEC,
                MainActivity.InterpolatorEnum.ACCELERATE_DEC.name),
        )
        val maps: MutableMap<KClass<*>, MutableMap<Int, out Any>> = mutableMapOf()

        val interpolatorCells =
            MainActivity.InterpolatorDelegateCell::class.createIntToTypeMap().putInto(maps)
        val someOtherCells = MainActivity.SomeOtherCell::class.createIntToTypeMap().putInto(maps)

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
                is MainActivity.InterpolatorDelegateCell -> 0
                is MainActivity.SomeOtherCell -> 1
                else -> Int.MAX_VALUE
            }
        }

        println(interpolatorCells)
        println(cellsViewTypes)
    }
}
