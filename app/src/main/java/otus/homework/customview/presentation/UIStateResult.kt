package otus.homework.customview.presentation

import otus.homework.customview.models.ProductPresentationModel

/**
 * Состояние получения данных по расходам
 *
 * @author Евтушенко Максим 24.12.2023
 */
sealed class UIStateResult {

    /**
     * Состояние отсутсвия запроса за данными
     * */
    object Idle : UIStateResult()


    /**
     * Успешно загруженный список продуктов клиента
     *
     * @param productList список продуктов клиента
     */
    data class ClientProductList(val productList: List<ProductPresentationModel>) : UIStateResult()
}
