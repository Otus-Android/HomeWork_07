package otus.homework.customview.domain

import kotlinx.serialization.Serializable
import otus.homework.customview.data.CategorySerializer

@Serializable(with = CategorySerializer::class)
enum class Category(val title: String) {
    GROCERIES("Продукты"),
    HEALTH("Здоровье"),
    CAFES_AND_RESTAURANTS("Кафе и рестораны"),
    ALCOHOL("Алкоголь"),
    FOOD_DELIVERY("Доставка еды"),
    TRANSPORT("Транспорт"),
    SPORTS("Спорт"),
    UNKNOWN("Прочее");

    companion object {
        fun fromString(value: String): Category {
            return when (value) {
                GROCERIES.title -> GROCERIES
                HEALTH.title -> HEALTH
                CAFES_AND_RESTAURANTS.title -> CAFES_AND_RESTAURANTS
                ALCOHOL.title -> ALCOHOL
                FOOD_DELIVERY.title -> FOOD_DELIVERY
                TRANSPORT.title -> TRANSPORT
                SPORTS.title -> SPORTS
                else -> UNKNOWN
            }
        }
    }
}