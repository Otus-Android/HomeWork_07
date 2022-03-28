package otus.homework.customview.model

import android.graphics.Color

data class Category(
    val name: String,
    val color: Int = Color.BLUE,
    val totalAmount: Int = 0
)