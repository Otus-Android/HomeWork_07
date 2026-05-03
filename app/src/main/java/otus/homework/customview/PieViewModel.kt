package otus.homework.customview

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import otus.homework.customview.data.JsonMapper

class PieViewModel(
    private val jsonMapper: JsonMapper
) : ViewModel() {

    val data = jsonMapper.mapJson()
        .map {
            CategoryModel(
                id = it.id,
                name = it.name,
                amount = it.amount,
                category = it.category,
            )
        }.groupBy { it.category }
}

class PieViewModelFactory(
    private val context: Context
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val jsonMapper = JsonMapper(context.applicationContext)
        return PieViewModel(jsonMapper) as T
    }
}