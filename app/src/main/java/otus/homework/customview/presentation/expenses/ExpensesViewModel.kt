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

class ExpensesViewModel(private val interactor: ExpensesInteractor) : ViewModel() {

    val uiState get() = _uiState.asStateFlow()
    private val _uiState: MutableStateFlow<ExpensesUiState> = MutableStateFlow(ExpensesUiState.IDLE)

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        _uiState.value = ExpensesUiState.Error(e.toString())
    }

    fun loadExpenses() {
        viewModelScope.launch(exceptionHandler) {
            _uiState.value = ExpensesUiState.Loading
            try {
                val expenses = interactor.getExpenses()
                _uiState.value = ExpensesUiState.Success(expenses)
            } catch (e: ExpensesException) {
                _uiState.value = ExpensesUiState.Error(e.toString())
            }
        }
    }

    companion object {

        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val diContainer = MyApplication.diContainer(application)
                return ExpensesViewModel(interactor = diContainer.interactor) as T
            }
        }
    }
}