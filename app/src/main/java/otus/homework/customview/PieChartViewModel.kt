package otus.homework.customview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class PieChartViewModel @Inject constructor(private val repository: JsonDataRepository) :
    ViewModel() {

    val payloads: LiveData<Map<String, Float>> = MutableLiveData()

    fun onInit() {
        (payloads as MutableLiveData).value =
            repository().groupBy { it.category }.mapValues { category ->
                category.value.sumOf { it.amount }
                    .toFloat()
            }
    }

}
