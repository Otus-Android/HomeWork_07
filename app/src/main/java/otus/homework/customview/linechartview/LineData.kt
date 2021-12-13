package otus.homework.customview.linechartview

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import otus.homework.customview.utils.createPaint
import otus.homework.customview.utils.getLocalDateFromLong
import java.time.LocalDate
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

@SuppressLint("NewApi")
class LineData(parcel: Parcel? = null) : Parcelable {

    var maxTime = 0L
    var minTime = Long.MAX_VALUE
    var maxAmount = 0.0

    /**
     * Map of category name to sorted by day set of [LineModel]
     */
    var lineModels = HashMap<String, LineModel>()

    fun add(
        name: String,
        amount: Double,
        time: Long,
        color: String? = null
    ) {
        val day = getLocalDateFromLong(time)
        if (lineModels.containsKey(name)) {
            lineModels[name]?.let { model ->
                val found = model.positions.find { it.day == day }
                if (found != null) {
                    found.amount += amount
                } else {
                    model.positions.add(DayPosition(
                        amount = amount,
                        day = day
                    ))
                }
            }
        } else {
            lineModels[name] = LineModel(
                category = name,
                positions = mutableListOf(DayPosition(amount, day)),
                paint = createPaint(color, lineModels.size, true)
            )
        }
        maxTime = max(maxTime, time)
        minTime = min(minTime, time)
        maxAmount = max(maxAmount, amount)
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeMap(lineModels)
        dest?.writeLong(maxTime)
        dest?.writeLong(minTime)
        dest?.writeDouble(maxAmount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LineData> {
        override fun createFromParcel(source: Parcel?): LineData {
            return LineData(source)
        }

        override fun newArray(size: Int): Array<LineData?> {
            return arrayOfNulls(size)
        }
    }

    init {
        lineModels =
            parcel?.readHashMap(null) as? HashMap<String, LineModel> ?: hashMapOf()
        maxTime = parcel?.readLong() ?: 0L
        minTime = parcel?.readLong() ?: Long.MAX_VALUE
        maxAmount = parcel?.readDouble() ?: 0.0
    }
}














