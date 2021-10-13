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
) : ViewModel() {

    private val _mutableStoresSharedFlow: MutableSharedFlow<List<Store>> = MutableSharedFlow()
    val storesSharedFlow: SharedFlow<List<Store>> = _mutableStoresSharedFlow
    private var stores: List<Store>? = null

    private val _mutableStoreGraphSharedFlow: MutableSharedFlow<List<Store>> = MutableSharedFlow()
    val storesGraphSharedFlow: SharedFlow<List<Store>> = _mutableStoreGraphSharedFlow
    private var storesGraph: List<Store>? = null

    fun getStoresFromJson(json: String, jsonGraph: String) {
        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                stores = adapter.fromJson(json)
                storesGraph = adapter.fromJson(jsonGraph)
            }

            stores?.let {
                _mutableStoresSharedFlow.emit(it)
            }

            storesGraph?.let {
                _mutableStoreGraphSharedFlow.emit(it)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class PieViewModelFactory(private val adapter: JsonAdapter<List<Store>>) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return PieViewModel(adapter) as T
    }
}