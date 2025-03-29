package otus.homework.customview.domain

import kotlinx.serialization.Serializable
import otus.homework.customview.data.CategorySerializer

@Serializable(with = CategorySerializer::class)
enum class Category {
    GROCERIES,
    HEALTH,
    CAFES_AND_RESTAURANTS,
    ALCOHOL,
    FOOD_DELIVERY,
    TRANSPORT,
    SPORTS,
    UNKNOWN;

    companion object {
        fun fromString(value: String): Category {
            return when (value) {
                "Продукты" -> GROCERIES
                "Здоровье" -> HEALTH
                "Кафе и рестораны" -> CAFES_AND_RESTAURANTS
                "Алкоголь" -> ALCOHOL
                "Доставка еды" -> FOOD_DELIVERY
                "Транспорт" -> TRANSPORT
                "Спорт" -> SPORTS
                else -> UNKNOWN
            }
        }
    }
}