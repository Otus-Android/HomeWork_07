package otus.homework.customview.models

/**
 * Доменная модель категории товара
 *
 * @author Евтушенко Максим 26.11.2023
 */
data class ProductDataModel(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Int
)