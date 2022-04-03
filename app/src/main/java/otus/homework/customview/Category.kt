package otus.homework.customview

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

enum class Category(
    val title: String,
    @ColorRes val colorRes: Int,
    @DrawableRes val iconRes: Int
) {
    PRODUCTS("Продукты", R.color.yellow_400, R.drawable.ic_shopping_basket),
    HEALTH("Здоровье", R.color.green_700, R.drawable.ic_health_and_safety),
    RESTAURANTS("Кафе и рестораны", R.color.purple_200, R.drawable.ic_restaurant),
    ALCOHOL("Алкоголь", R.color.pink_900, R.drawable.ic_wine),
    DELIVERY("Доставка еды", R.color.light_green_200, R.drawable.ic_delivery_dining),
    TRANSPORT("Транспорт", R.color.red_700, R.drawable.ic_trasnport),
    SPORT("Спорт", R.color.teal_200, R.drawable.ic_sports),
    OTHER("Другое", R.color.gray_300, R.drawable.ic_more);

    companion object {
        fun String.toCategory(): Category = values().firstOrNull { it.title == this } ?: OTHER
    }
}