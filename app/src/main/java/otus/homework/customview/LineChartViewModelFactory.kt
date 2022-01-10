package otus.homework.customview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import otus.homework.customview.di.ActivityScope
import javax.inject.Inject
import javax.inject.Provider

@ActivityScope
class LineChartViewModelFactory @Inject constructor(private val lineChartViewModelProvider: Provider<LineChartViewModel>) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return lineChartViewModelProvider.get() as T
    }
}