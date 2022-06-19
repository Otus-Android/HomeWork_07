package otus.homework.customview

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Payload(
    val companies: List<Company>
)

@Serializable
data class Company(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: Category,
    val time: Long
)

@Serializable
enum class Category(val id: Int) {
    @SerialName("Продукты")
    PRODUCTS(0),

    @SerialName("Здоровье")
    HEALTH(1),

    @SerialName("Кафе и рестораны")
    CAFE(2),

    @SerialName("Алкоголь")
    ALCOHOL(3),

    @SerialName("Доставка еды")
    FOOD_DELIVERY(4),

    @SerialName("Транспорт")
    TRANSPORT(5),

    @SerialName("Спорт")
    SPORT(6)
}