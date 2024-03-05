package otus.homework.customview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import otus.homework.customview.chart.ChartData
import java.io.BufferedReader

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _chartData = MutableStateFlow<List<ChartData>>(emptyList())
    val chartData: StateFlow<List<ChartData>> = _chartData.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _chartData.value = application.resources.openRawResource(R.raw.payload).use {
                it.bufferedReader().use(BufferedReader::readText)
            }.let {
                Json.decodeFromString(it)
            }
        }
    }

}