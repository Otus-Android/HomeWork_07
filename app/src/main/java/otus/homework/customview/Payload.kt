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
    val time: Int
)

@Serializable
enum class Category {
    @SerialName("Продукты")
    PRODUCTS,
    @SerialName("Здоровье")
    HEALTH,
    @SerialName("Кафе и рестораны")
    CAFE,
    @SerialName("Алкоголь")
    ALCOHOL,
    @SerialName("Доставка еды")
    FOOD_DELIVERY,
    @SerialName("Транспорт")
    TRANSPORT,
    @SerialName("Спорт")
    SPORT
}