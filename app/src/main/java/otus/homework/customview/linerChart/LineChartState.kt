package otus.homework.customview.linerChart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import otus.homework.customview.JsonModel

@Parcelize
class LineChartState : Parcelable {

    var data: Map<String, List<JsonModel>> = emptyMap()

}