package otus.homework.customview.domain

import otus.homework.customview.models.ProductDataModel

/**
 * Репозиторий для получения списка продуктов клиента
 *
 * @author Евтушенко Максим 16.12.2023
 */
interface ClientProductsRepository {

    /**
     * Загрузка списка продуктов клиента
     */
    fun loadClientCategories():List<ProductDataModel>
}