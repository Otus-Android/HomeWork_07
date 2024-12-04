package otus.homework.customview.PieChart

import android.os.Parcelable
import android.view.View


/**
 * Собственный state для сохранения и восстановление данных
 */
class PieChartState(
    private val superSavedState: Parcelable?,
    val dataList: List<PieChartData>
) : View.BaseSavedState(superSavedState), Parcelable