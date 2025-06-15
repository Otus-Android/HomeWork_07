package otus.homework.customview

import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Test
import otus.homework.customview.task_1.model.Category
import otus.homework.customview.task_1.model.Data
import otus.homework.customview.util.CategoryDeserializer

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

    @Test
    fun categoryDeserializationTest() {
        val jsonString = """
            {"category": "Здоровье"}
            """.trimIndent()

        val category = CategoryClass(Category.HEALTH)

        val gson = GsonBuilder()
            .registerTypeAdapter(Category::class.java, CategoryDeserializer())
            .create()

        val categoryFromJsonString = gson.fromJson(jsonString, CategoryClass::class.java)

        assertEquals(category, categoryFromJsonString)
    }


    @Test
    fun dataDeserializationTest() {
        val jsonString = """[
  {
    "id": 1,
    "name": "Азбука Вкуса",
    "amount": 1580,
    "category": "Продукты",
    "time": 1623318531
  },
  {
    "id": 2,
    "name": "Ригла",
    "amount": 499,
    "category": "Здоровье",
    "time": 1623322251
  },
  {
    "id": 3,
    "name": "Пятерочка",
    "amount": 129,
    "category": "Продукты",
    "time": 1623322371
  },
  {
    "id": 4,
    "name": "Truffo",
    "amount": 4541,
    "category": "Кафе и рестораны",
    "time": 1623326031
  },
  {
    "id": 5,
    "name": "Simple Wine",
    "amount": 1600,
    "category": "Алкоголь",
    "time": 1623329631
  },
  {
    "id": 6,
    "name": "Азбука Вкуса Экспресс",
    "amount": 1841,
    "category": "Доставка еды",
    "time": 1623322371
  },
  {
    "id": 7,
    "name": "Uber",
    "amount": 369,
    "category": "Транспорт",
    "time": 1623416031
  },
  {
    "id": 8,
    "name": "Метро",
    "amount": 100,
    "category": "Транспорт",
    "time": 1623416211
  },
  {
    "id": 9,
    "name": "Стоматология",
    "amount": 8000,
    "category": "Здоровье",
    "time": 1623419811
  },
  {
    "id": 10,
    "name": "Пятерочка",
    "amount": 809,
    "category": "Продукты",
    "time": 1623419934
  },
  {
    "id": 11,
    "name": "Бассейн",
    "amount": 1000,
    "category": "Спорт",
    "time": 1623419934
  },
  {
    "id": 12,
    "name": "Uber",
    "amount": 389,
    "category": "Транспорт",
    "time": 1623419934
  }
]"""
        val gson = GsonBuilder()
            .registerTypeAdapter(Category::class.java, CategoryDeserializer())
            .create()

        val tempList = gson.fromJson(jsonString, Array<Data>::class.java)
        println(tempList.asList())
        assertEquals(tempList.size, 12)
        assertEquals(tempList[0].category, Category.PRODUCTS)
    }


    @Test
    fun preparedDataForCanvasTest() {
        val jsonString = """[
  {
    "id": 1,
    "name": "Азбука Вкуса",
    "amount": 1580,
    "category": "Продукты",
    "time": 1623318531
  },
  {
    "id": 2,
    "name": "Ригла",
    "amount": 499,
    "category": "Здоровье",
    "time": 1623322251
  },
  {
    "id": 3,
    "name": "Пятерочка",
    "amount": 129,
    "category": "Продукты",
    "time": 1623322371
  },
  {
    "id": 4,
    "name": "Truffo",
    "amount": 4541,
    "category": "Кафе и рестораны",
    "time": 1623326031
  },
  {
    "id": 5,
    "name": "Simple Wine",
    "amount": 1600,
    "category": "Алкоголь",
    "time": 1623329631
  },
  {
    "id": 6,
    "name": "Азбука Вкуса Экспресс",
    "amount": 1841,
    "category": "Доставка еды",
    "time": 1623322371
  },
  {
    "id": 7,
    "name": "Uber",
    "amount": 369,
    "category": "Транспорт",
    "time": 1623416031
  },
  {
    "id": 8,
    "name": "Метро",
    "amount": 100,
    "category": "Транспорт",
    "time": 1623416211
  },
  {
    "id": 9,
    "name": "Стоматология",
    "amount": 8000,
    "category": "Здоровье",
    "time": 1623419811
  },
  {
    "id": 10,
    "name": "Пятерочка",
    "amount": 809,
    "category": "Продукты",
    "time": 1623419934
  },
  {
    "id": 11,
    "name": "Бассейн",
    "amount": 1000,
    "category": "Спорт",
    "time": 1623419934
  },
  {
    "id": 12,
    "name": "Uber",
    "amount": 389,
    "category": "Транспорт",
    "time": 1623419934
  }
]"""
        val preparedDataForCanvas: MutableMap<Category, Float> = mutableMapOf()
        val gson = GsonBuilder()
            .registerTypeAdapter(Category::class.java, CategoryDeserializer())
            .create()

        val dataList = gson.fromJson(jsonString, Array<Data>::class.java).asList()

        val amountInOneDegree = dataList.sumOf { it.amount } / 360f
        dataList.groupBy { it.category }.forEach { (category, data) ->
            preparedDataForCanvas[category] = data.sumOf { it.amount }.toFloat() / amountInOneDegree
        }

        assertEquals(7, preparedDataForCanvas.size)
        assertEquals(1000f / amountInOneDegree, preparedDataForCanvas[Category.SPORT])
    }
}

data class CategoryClass(val category: Category)