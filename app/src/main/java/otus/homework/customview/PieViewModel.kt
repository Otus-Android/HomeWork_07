package otus.homework.customview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PieViewModel(
    private val adapter: JsonAdapter<List<Store>>
): ViewModel() {
    private val mutableSharedFlow: MutableSharedFlow<List<Store>> = MutableSharedFlow()
    val sharedFlow: SharedFlow<List<Store>> = mutableSharedFlow

    private var stores: List<Store>? = null

    fun getStoresFromJson(json: String) {
        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                stores = adapter.fromJson(json)
            }

            stores?.let {
                mutableSharedFlow.emit(it)
            }
        }
    }

}

class PieViewModelFactory(private val adapter: JsonAdapter<List<Store>>):
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PieViewModel(adapter) as T
    }
}