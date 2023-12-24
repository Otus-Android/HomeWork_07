package otus.homework.customview.domain

import otus.homework.customview.models.ProductDataModel
import otus.homework.customview.models.ProductDomainModel

/**
 * Интерактор для получения списка продуктов клиента
 *
 * @author Евтушенко Максим 16.12.2023
 */
class ClientProductsInteractor(private val clientProductsRepository: ClientProductsRepository) {

    /**
     * Загрузка списка продуктов клиента
     */
    fun loadClientCategories(): List<ProductDomainModel> {
        val dataCategories = clientProductsRepository.loadClientCategories()
        return convertDataToDomain(dataCategories)
    }

    private fun convertDataToDomain(dataCategories: List<ProductDataModel>): List<ProductDomainModel> {
        val result = mutableListOf<ProductDomainModel>()
        var totalAmount = 0f
        dataCategories.forEach {
            totalAmount += it.amount
        }

        var previousPercentRation = 0f
        dataCategories.forEach {
            val amount = it.amount
            val percentRation = (amount.toFloat() / totalAmount) * 100f
            result.add(
                ProductDomainModel(
                    name = it.name,
                    amount = amount,
                    category = it.category,
                    percentRatio = percentRation,
                    previousPercentRation = previousPercentRation
                )
            )
            previousPercentRation += percentRation
        }
        return result
    }
}