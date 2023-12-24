package otus.homework.customview.models

/**
 * Доменная модель категории товара
 *
 * @param name название продукта
 * @param amount кол-во единиц купленного продукта
 * @param category категория продукта
 * @param percentRatio процентное соотношение продукта из всех купленных продуктов
 * @param previousPercentRation процентное соотношение предыдущего продукта из всех купленных продуктов
 * (для поиска точки старта на круговой диаграмме)
 *
 * @author Евтушенко Максим 26.11.2023
 */
data class ProductDomainModel(
    val name: String,
    val amount: Int,
    val category: String,
    val percentRatio: Float,
    val previousPercentRation: Float
)