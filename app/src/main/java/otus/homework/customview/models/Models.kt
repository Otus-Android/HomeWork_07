package otus.homework.customview.models

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import otus.homework.customview.utils.ColorGenerator

data class Expenditure(
    val id: Int,
    val name: String,
    val amount: Float,
    val category: String,
    val time: Long
)

@Parcelize
data class PieChartSegment(
    val id: Int,
    val name: String,
    val amount: Float,
    val category: String,
    val startAngle: Float,
    val endAngle: Float,
    val percentageOfMaximum: Float,
    @ColorInt val color: Int
): Parcelable {

    @IgnoredOnParcel
    val segmentAngle = endAngle - startAngle
}

@Parcelize
data class LinearChartPoint(
    val id: Int,
    val name: String,
    val amount: Float,
    val category: String,
    val time: Long,
    val dayInMonth: Int
) : Parcelable {

    fun mapExpenditureCategory() = when (category) {
        "Продукты" -> ExpenditureCategory.PRODUCTS
        "Здоровье" -> ExpenditureCategory.HEALTH
        "Кафе и рестораны" -> ExpenditureCategory.EATING_OUT
        "Алкоголь" -> ExpenditureCategory.ALCOHOL
        "Доставка еды" -> ExpenditureCategory.DELIVERY
        "Транспорт" -> ExpenditureCategory.TRANSPORT
        "Спорт" -> ExpenditureCategory.SPORT
        else -> throw IllegalArgumentException("Error while parsing unknown category: $category")
    }
}

@Parcelize
enum class ExpenditureCategory : Parcelable {
    PRODUCTS, HEALTH, EATING_OUT, ALCOHOL, DELIVERY, TRANSPORT, SPORT;

    @ColorInt
    fun getChartColor(): Int {
        return when (this) {
            PRODUCTS -> Color.rgb(38, 50, 124)
            HEALTH -> Color.rgb(100, 188, 223)
            EATING_OUT -> Color.rgb(230, 197, 87)
            TRANSPORT -> Color.rgb(202, 58, 46)
            else -> ColorGenerator.generateColor()
        }
    }
}

enum class Chart {
    PIE, LINEAR
}