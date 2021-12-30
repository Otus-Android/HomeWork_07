package otus.homework.customview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import otus.homework.customview.di.ActivityScope
import javax.inject.Inject
import javax.inject.Provider

@ActivityScope
class PieChartViewModelFactory @Inject constructor(private val pieChartViewModelProvider: Provider<PieChartViewModel>) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return pieChartViewModelProvider.get() as T
    }
}