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
import otus.homework.customview.data.ExpensesException
import otus.homework.customview.domain.ExpensesInteractor
import otus.homework.customview.domain.config.ExpensesConfig
import otus.homework.customview.domain.config.ExpensesProvider

class ExpensesViewModel(
    private val interactor: ExpensesInteractor,
    private val config: ExpensesConfig
) : ViewModel() {

    val uiState get() = _uiState.asStateFlow()
    private val _uiState: MutableStateFlow<ExpensesUiState> = MutableStateFlow(ExpensesUiState.IDLE)

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _uiState.value = ExpensesUiState.Error(e.toString())
    }

    private var max: Int? = null

    fun loadExpenses() {
        viewModelScope.launch(exceptionHandler) {
            _uiState.value = ExpensesUiState.Loading
            try {
                val expenses = if (max == null) interactor.getExpenses() else {
                    interactor.getExpenses(max)
                }
                _uiState.value = ExpensesUiState.Success(expenses)
            } catch (e: ExpensesException) {
                _uiState.value = ExpensesUiState.Error(e.toString())
            }
        }
    }

    fun onSourceChanged(isChecked: Boolean) {
        config.provider = if (isChecked) ExpensesProvider.RANDOM else ExpensesProvider.LOCAL
        loadExpenses()
    }

    fun onMaxChanged(max: String) {
        this.max = max.toIntOrNull()
    }

    companion object {

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