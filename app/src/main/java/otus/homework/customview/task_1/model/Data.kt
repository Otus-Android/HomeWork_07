package otus.homework.customview.task_1.model

data class Data(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: Category,
    val time: Long,
)

enum class Category (val categoryName: String) {
    HEALTH("Здоровье"),
    TRANSPORT("Транспорт"),
    PRODUCTS("Продукты"),
    CAFES_AND_RESTAURANTS("Кафе и рестораны"),
    ALCOHOL("Алкоголь"),
    DELIVERY("Доставка еды"),
    SPORT("Спорт"),
    HOTELS("Отели"),
    CINEMA("Кино"),
    CLOTHES("Одежда");

    companion object {
        fun nameOf(categoryName: String): Category {
            return when (categoryName) {
                "Здоровье" -> HEALTH
                "Транспорт" -> TRANSPORT
                "Продукты" -> PRODUCTS
                "Кафе и рестораны" -> CAFES_AND_RESTAURANTS
                "Алкоголь" -> ALCOHOL
                "Доставка еды" -> DELIVERY
                "Спорт" -> SPORT
                "Отели" -> HOTELS
                "Кино" -> CINEMA
                "Одежда" -> CLOTHES
                else -> error("Unknown category $categoryName")
            }
        }
    }
}
