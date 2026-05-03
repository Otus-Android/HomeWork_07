package otus.homework.customview

import java.io.Serializable


data class CategoryModel(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
) : Serializable
