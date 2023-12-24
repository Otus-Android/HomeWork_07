package otus.homework.customview.presentation

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import otus.homework.customview.Event
import otus.homework.customview.domain.ClientProductsInteractor
import otus.homework.customview.models.ProductDomainModel
import otus.homework.customview.models.ProductPresentationModel
import kotlin.random.Random

/**
 * ViewModel для загрузки данных клиента
 *
 * @author Евтушенко Максим 16.12.2023
 */
class MainActivityViewModel(
    private val interactor: ClientProductsInteractor
) : ViewModel() {

    private val _uiState = MutableStateFlow<UIStateResult>(UIStateResult.Idle)
    val uiState: StateFlow<UIStateResult> = _uiState

    /**
     * Загрузка продуктов для построения круговой диаграммы
     */
    fun loadProducts() {
        val domainProducts = interactor.loadClientCategories()
        val presentationProducts = convertDomainToPresentation(domainProducts)
        _uiState.tryEmit(UIStateResult.ClientProductList(presentationProducts))
    }

    private fun convertDomainToPresentation(domainProducts: List<ProductDomainModel>): List<ProductPresentationModel> {
        val result = mutableListOf<ProductPresentationModel>()

        val rnd = Random.Default
        domainProducts.forEach {
            val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            result.add(
                ProductPresentationModel(
                    percentRatio = it.percentRatio,
                    previousPercentRation = it.previousPercentRation,
                    lineColor = color,
                    stroke = 40,
                    event = Event.ProductClick(it.category)
                )
            )
        }
        return result
    }
}

class MainActivityViewModelFactory(
    private val interactor: ClientProductsInteractor
) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MainActivityViewModel(interactor) as T
}