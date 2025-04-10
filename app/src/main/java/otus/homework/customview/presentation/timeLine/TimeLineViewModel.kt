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
import otus.homework.customview.presentation.timeLine.model.SpendModel
import otus.homework.customview.presentation.timeLine.model.TimeLineData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TimeLineViewModel(
    getSpendsUseCase: GetSpendsUseCase,
): ViewModel() {

    private val selectedCategoryFlow = MutableStateFlow(Category.HEALTH)
    private val _spendsInTimeLine = MutableStateFlow(emptyList<Spend>())

    val categories = MutableStateFlow(emptyList<String>())

    val spendsInTimeLine: Flow<TimeLineData> = combine(selectedCategoryFlow, _spendsInTimeLine) { selectedCategory, spends ->
        val availableCategory = spends.map { it.category }.toSet()
        categories.value = availableCategory.map { it.title }
        val maxAmount = spends.maxOf { it.amount }
        val times = spends.map { it.time }.sorted()
        val spendModels = spends.map { it.toSpendModel(times.indexOf(it.time), maxAmount) }.also { println("XXX $it") }
        val days = spendModels.map { it.date }.sorted().toSet()
        TimeLineData(
            selected = selectedCategory,
            availableCategories = availableCategory,
            spends = spendModels.groupBy { it.category },
            totalDays = days,
            maxAmount = maxAmount
        )
    }


    init {
        getSpendsUseCase.getSpends()
            .onEach { spends ->
                _spendsInTimeLine.value = spends
            }
            .launchIn(viewModelScope)
    }

    fun changeSelected(index: Int) {
        val category = categories.value.getOrNull(index) ?: return
        selectedCategoryFlow.value = Category.fromString(category)
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

private fun Spend.toSpendModel(index: Int, maxAmount: Int): SpendModel {
    return SpendModel(
        name = name,
        amount = amount,
        category = category,
        date = localDateTime(time),
        part = Pair(index.toFloat(), amount.toFloat() / maxAmount)
    )
}

private fun localDateTime(seconds: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = seconds * 1000
    return SimpleDateFormat("dd MMM", Locale.getDefault()).format(calendar.time)
}
