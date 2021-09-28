package otus.homework.customview.piechartview

import android.graphics.Color
import androidx.annotation.ColorRes
import otus.homework.customview.R

/**
 *
 *
 * @author Юрий Польщиков on 27.09.2021
 */
enum class Category(
    val title: String,
    @ColorRes val color: Int
) {
    GROCERY("Продукты", R.color.red_400),
    HEALTH("Здоровье", R.color.pink_400),
    CAFE("Кафе и рестораны", R.color.purple_400),
    ALCOHOL("Алкоголь", R.color.deep_purple_400),
    FOOD_DELIVERY("Доставка еды", R.color.indigo_400),
    TRANSPORT("Транспорт", R.color.blue_400),
    SPORT("Спорт", R.color.light_blue_400),

    OTHER("Другое", Color.GRAY);

    companion object {
        fun from(title: String): Category {
            for (category in values()) {
                if (category.title == title) {
                    return category
                }
            }
            return OTHER
        }
    }
}
