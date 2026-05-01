package otus.homework.customview.chart

import android.os.Parcelable
import android.view.View.BaseSavedState
import kotlinx.parcelize.Parcelize

/**
 * Класс для сохранения состояния [PieChartView].
 */
@Parcelize
internal class PieChartSavedState(
    private val sourceState: Parcelable?,
    val data: List<PieData>,
    val selectedCategory: String?
) : BaseSavedState(sourceState)
