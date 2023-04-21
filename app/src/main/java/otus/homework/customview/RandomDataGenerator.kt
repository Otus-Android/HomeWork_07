package otus.homework.customview

import java.util.*
import kotlin.random.Random

object RandomDataGenerator {

    private const val CATEGORIES_AMOUNT_MIN = 5
    private const val CATEGORIES_AMOUNT_MAX = 10

    private const val PLACES_AMOUNT_MIN = 5
    private const val PLACES_AMOUNT_MAX = 10

    private const val MONEY_AMOUNT_MIN = 100
    private const val MONEY_AMOUNT_MAX = 1000

    private const val POINTS_AMOUNT_MIN = 50
    private const val POINTS_AMOUNT_MAX = 100



    private var expenseId = 0

    fun generateRandomData(): MutableList<Expense> {

        val randomDataList = mutableListOf<Expense>()

        val randomHashMap = generateRandomMap()

        repeat((POINTS_AMOUNT_MIN..POINTS_AMOUNT_MAX).random()) {
            val category = randomHashMap.keys.random()
            randomDataList.add(
                Expense(
                    id = expenseId++,
                    name = randomHashMap[category]!!.random(),
                    amount = (MONEY_AMOUNT_MIN..MONEY_AMOUNT_MAX).random(),
                    category = category,
                    time = getRandomTime()
                )
            )
        }
        return randomDataList
    }

    private fun getRandomTime(): Int {
        return (Date().time / 1000 + (-30..30).random() * 86400).toInt()
    }

    private fun generateRandomMap(): HashMap<String, List<String>> {
        val randomMap = hashMapOf<String, List<String>>()
        val randomStringList = generateRandomList()
        randomStringList.map { randomString ->
            repeat((PLACES_AMOUNT_MIN..PLACES_AMOUNT_MAX).random()) {
                randomMap[randomString] = generateRandomList()
            }
        }
        return randomMap
    }

    private fun generateRandomList(): List<String> {
        val randomStringList = mutableListOf<String>()
        repeat((CATEGORIES_AMOUNT_MIN..CATEGORIES_AMOUNT_MAX).random()) {
            randomStringList.add(generateRandomString())
        }
        return randomStringList
    }

    private fun generateRandomString(): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..5)
            .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
            .joinToString("")
    }
}