package otus.homework.customview.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import otus.homework.customview.MyApplication
import otus.homework.customview.domain.ExpensesInteractor
import otus.homework.customview.domain.config.ExpensesConfig
import otus.homework.customview.domain.config.ExpensesProviderType
import otus.homework.customview.domain.models.ExpensesException

/**
 * `ViewModel` данных по расходам
 *
 * @param interactor интерактор данных по расходам
 * @param config конфигурационные данные по расходам
 */
class ExpensesViewModel(
    private val interactor: ExpensesInteractor, private val config: ExpensesConfig
) : ViewModel() {

    /** Состояние получения данных по расходам */
    val uiState get() = _uiState.asStateFlow()
    private val _uiState: MutableStateFlow<ExpensesUiState> = MutableStateFlow(ExpensesUiState.Idle)

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _uiState.value = ExpensesUiState.Error(e.toString())
    }

    private var max: Int? = null

    /** Загрузить данные по расходам */
    fun loadExpenses() = getCategories(false)

    /** Обновить данные по расходам */
    fun updateExpenses() = getCategories(true)

    private fun getCategories(force: Boolean) {
        viewModelScope.launch(exceptionHandler) {
            _uiState.value = ExpensesUiState.Loading
            try {
                val expenses = interactor.getCategories(maxExpenses = max, force = force)
                _uiState.value = ExpensesUiState.Success(expenses)
            } catch (e: ExpensesException) {
                _uiState.value = ExpensesUiState.Error(e.toString())
            }
        }
    }

    /**
     * Обработать нажатие на переключатель источника данных
     *
     * @param isChecked признак выбора источника сгенерированных данных
     */
    fun onSourceChanged(isChecked: Boolean) {
        val provider = if (isChecked) ExpensesProviderType.RANDOM else ExpensesProviderType.FILE
        if (config.providerType != provider) {
            config.providerType = provider
            updateExpenses()
        }
    }

    /**
     * Обработать изменение максимального кол-ва возможных записей по расходам [max]
     */
    fun onMaxChanged(max: String) {
        this.max = max.toIntOrNull()
    }

    companion object {

        /** Фабрика создания `ViewModel` */
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val diContainer = MyApplication.diContainer(application)
                return ExpensesViewModel(
                    interactor = diContainer.interactor, config = diContainer.config
                ) as T
            }
        }
    }
}