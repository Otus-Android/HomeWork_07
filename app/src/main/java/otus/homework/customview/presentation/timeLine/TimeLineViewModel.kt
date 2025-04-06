package otus.homework.customview.presentation.timeLine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import otus.homework.customview.CustomViewApp
import otus.homework.customview.domain.Category
import otus.homework.customview.domain.GetSpendsUseCase
import otus.homework.customview.domain.Spend
import otus.homework.customview.presentation.timeLine.model.TimeLineData

class TimeLineViewModel(
    getSpendsUseCase: GetSpendsUseCase,
): ViewModel() {

    private val selectedCategoryFlow = MutableStateFlow(Category.SPORTS)
    private val _spendsInTimeLine = MutableStateFlow(emptyList<Spend>())

    val spendsInTimeLine: Flow<TimeLineData> = combine(selectedCategoryFlow, _spendsInTimeLine) { selectedCategory, spends ->
        val availableCategory = spends.map { it.category }.toSet()
        TimeLineData(selectedCategory, availableCategory, spends)
    }

    init {
        getSpendsUseCase.getSpends()
            .onEach { spends ->
                _spendsInTimeLine.value = spends
            }
            .launchIn(viewModelScope)
    }

    fun changeSelected(category: Category) {
        selectedCategoryFlow.value = category
    }

    companion object {
        val factory = object: ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY]) as CustomViewApp
                return TimeLineViewModel(GetSpendsUseCase(application.repository)) as T
            }
        }
    }
}