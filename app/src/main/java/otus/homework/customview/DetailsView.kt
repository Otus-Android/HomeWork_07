package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.os.Build.VERSION
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import otus.homework.customview.pojo.Details
import otus.homework.customview.pojo.GraphsBuildDetailsData
import otus.homework.customview.util.ChartDefaultDataCreator
import otus.homework.customview.util.Converter
import java.util.*

class DetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var _isOpened = false
    val isOpened: Boolean get() = _isOpened

    // Category details
    private lateinit var _category: String
    private var _categoryColor: Int = Color.BLACK
    private lateinit var _detailsCategory: List<Details>
    private lateinit var _rangeDateAll: Pair<Long, Long>
    private lateinit var _rangeAmountAll: Pair<Int, Int>

    // Axis data
    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f

    private var divisionX = 0f
    private var divisionY = 0f
    private var axisDates = sortedMapOf<Long, Float>()  //dates on axis
    private var axisAmounts = sortedMapOf<Int, Float>()   //amounts on axis

    private var verticalKoef = 0f

    private var axisHorizontalTextWidth = 0f
    private var axisVerticalTextWidth = 0f
    private var titleWidth = 0f
    private var axisTitleWidth = 0f

    private val paintAxis = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        textSize = AXIS_TEXT_HEIGHT
    }.also {
        axisHorizontalTextWidth = it.measureText("XX.XX.XXXX")
        axisVerticalTextWidth = it.measureText("0000")
        axisTitleWidth = it.measureText("Количество")
    }

    private val paintGrid = Paint().apply {
        color = ChartDefaultDataCreator.getGridColor()
    }

    private val paintTitle = Paint().apply {
        color = Color.BLACK
        strokeWidth = 10f
        textSize = TITLE_TEXT_HEIGHT
    }

    private val paintBar = Paint().apply {
        color = Color.BLACK
        strokeWidth = BAR_WIDTH
    }

    private val axisPath = Path()

    private val linesDivisionsX = mutableListOf<Float>()
    private val linesDivisionsY = mutableListOf<Float>()

    private var count = 0

    fun populate(graphBuildDetailsData: GraphsBuildDetailsData) {
        open()
        with(graphBuildDetailsData) {
            _category = category
            _detailsCategory = detailsCategory
            _rangeDateAll = rageDate
            _rangeAmountAll = rageAmount
            _categoryColor = color
        }

        populateHorizontalDataMapKeys()
        populateVerticalDataMapKeys()

        titleWidth = paintTitle.measureText(_category)

        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        setAxisAndGridData(width, height)

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        drawTitle(canvas)
        drawAxis(canvas)
        drawBars(canvas)
    }

    private fun setAxisAndGridData(width: Int, height: Int) {
        startX = START_OFFSET
        startY = height - START_OFFSET
        endX = width - START_OFFSET
        endY = START_OFFSET

        divisionX = (endX - startX) / axisDates.size
        divisionY = (startY - endY) / axisAmounts.size

        populateHorizontalDataMapValues()

        populateVerticalDataMapValues()

        verticalKoef = axisAmounts[axisAmounts.lastKey()]!! / axisAmounts.lastKey()
    }

    private fun populateHorizontalDataMapKeys() {
        val dayList = Converter.rangeTimestampToDaysList(_rangeDateAll)
        axisDates[dayList.first() - 1] = 0f
        dayList.forEach {
            axisDates[it] = 0f
        }
    }

    private fun populateVerticalDataMapKeys() {
        val amountsOnAxisList =
            Converter.rangeIntToIntListAccordingDivision(
                _rangeAmountAll.second,
                GRID_DIVISION_QUANTITY
            )

        amountsOnAxisList.forEach {
            axisAmounts[it] = 0f
        }
    }

    private fun populateHorizontalDataMapValues() {
        count = 0
        for ((k, _) in axisDates) {
            axisDates[k] = count * divisionX + startX
            val lineItem =
                listOf(axisDates[k]!!, startY, axisDates[k]!!, startY - DIVISION_MARK_LENGTH)
            linesDivisionsX.addAll(count * 4, lineItem)
            count++
        }
    }

    private fun populateVerticalDataMapValues() {
        count = 0
        for ((k, v) in axisAmounts) {
            axisAmounts[k] = count * divisionY
            val lineItem =
                listOf(startX, startY - axisAmounts[k]!!, startX + DIVISION_MARK_LENGTH, startY - axisAmounts[k]!!)
            linesDivisionsY.addAll(count * 4, lineItem)
            count++
        }
    }

    private fun drawAxis(canvas: Canvas) {
        canvas.drawLine(startX, startY, endX, startY, paintAxis)
        canvas.drawLine(startX, startY, startX, endY, paintAxis)
        canvas.drawLines(linesDivisionsX.toFloatArray(), paintAxis)
        canvas.drawLines(linesDivisionsY.toFloatArray(), paintAxis)

        drawGrid(canvas)
        drawHorizontalDivisionsText(canvas)
        drawVerticalDivisionsText(canvas)

        drawVerticalAxisName(canvas)
    }

    private fun drawTitle(canvas: Canvas) {
        canvas.drawText(
            _category,
            width.toFloat() / 2 - titleWidth / 2,
            TITLE_TEXT_HEIGHT,
            paintTitle
        )
    }

    private fun drawHorizontalDivisionsText(canvas: Canvas) {
        for ((k,v) in axisDates) {
            val textDate = Converter.timestampToDateString(k * 3600 * 24)
            canvas.drawText(
                textDate,
                v - axisHorizontalTextWidth / 3 + DEFAULT_SPACE,
                startY + AXIS_TEXT_HEIGHT + DEFAULT_SPACE,
                paintAxis.apply {
                    color = Color.BLACK
                    textScaleX = 0.8f
                    textSize = AXIS_TEXT_HEIGHT
                })
        }
    }

    private fun drawVerticalDivisionsText(canvas: Canvas) {
        for ((k,v) in axisAmounts) {
            val textAmount = k.toString()
            canvas.drawText(
                textAmount,
                startX + DEFAULT_SPACE,
                startY - v - DEFAULT_SPACE,
                paintAxis.apply {
                    color = Color.BLACK
                    textSize = AXIS_TEXT_HEIGHT
                }
            )
        }
    }

    private fun drawGrid(canvas: Canvas) {
        axisAmounts.values.forEach {
            canvas.drawLine(
                startX,
                startY - it,
                endX,
                startY - it,
                paintGrid)
        }
    }

    private fun drawVerticalAxisName(canvas: Canvas) {
        axisPath.apply {
            reset()
            moveTo(startX, startY)
            lineTo(startX, endY)
        }
        canvas.drawTextOnPath("Количество",
            axisPath,
            height.toFloat() / 2 - axisVerticalTextWidth,
            -DEFAULT_SPACE,
            paintAxis.apply { textSize = 1.5f * AXIS_TEXT_HEIGHT })
    }

    private fun drawBars(canvas: Canvas) {
        _detailsCategory.forEach {
            canvas.drawLine(
                axisDates[it.date]!!,
                startY,
                axisDates[it.date]!!,
                startY - it.amount * verticalKoef,
                paintBar.apply { color = _categoryColor })
        }
    }

    fun open() {
        if (isOpened) return
        _isOpened = true
        visibility = VISIBLE
    }

    fun close() {
        if (!isOpened) return
        _isOpened = false
        visibility = GONE
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.apply {
            ssIsOpened = isOpened
            ssCategory = _category
            ssCategoryColor = _categoryColor
            ssDetailsCategory = ArrayList(_detailsCategory)
            ssAxisDates = axisDates
            ssAxisAmounts = axisAmounts
        }
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state is SavedState) {
            with(state) {
                _isOpened = ssIsOpened
                _category = ssCategory ?: ""
                _categoryColor = ssCategoryColor
                _detailsCategory = ssDetailsCategory as List<Details>
                axisDates = ssAxisDates as SortedMap<Long, Float>
                axisAmounts = ssAxisAmounts as SortedMap<Int, Float>
            }
            visibility = if (isOpened) VISIBLE else GONE
        }
    }

    companion object {
        const val START_OFFSET = 80f
        const val GRID_DIVISION_QUANTITY = 20
        const val DIVISION_MARK_LENGTH = 40f
        const val AXIS_TEXT_HEIGHT = 40f
        const val TITLE_TEXT_HEIGHT = 80f
        const val BAR_WIDTH = 50f
        const val DEFAULT_SPACE = 10f
    }

    private class SavedState : BaseSavedState, Parcelable {
        var ssIsOpened: Boolean = false
        var ssCategory: String? = null
        var ssCategoryColor = Color.BLACK
        var ssDetailsCategory: ArrayList<Details>? = null
        var ssAxisDates: Map<Long, Float>? = null
        var ssAxisAmounts: Map<Int, Float>? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(src: Parcel) : super(src) {
            ssIsOpened = src.readInt() == 1
            ssCategory = src.readString()
            ssCategoryColor = src.readInt()
            if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ssDetailsCategory = src.readArrayList(arrayListOf<Details>().javaClass.classLoader, Details::class.java)
                ssAxisDates =
                    src.readHashMap(mapOf<Long,Float>().javaClass.classLoader, Long::class.java, Float::class.java) as SortedMap<Long, Float>
                ssAxisAmounts =
                    src.readHashMap(mapOf<Int,Float>().javaClass.classLoader, Int::class.java, Float::class.java) as SortedMap<Int, Float>
            } else {
                ssDetailsCategory = src.readArrayList(arrayListOf<Details>().javaClass.classLoader) as ArrayList<Details>?
                ssAxisDates = src.readHashMap(mapOf<Long,Float>().javaClass.classLoader) as SortedMap<Long, Float>
                ssAxisAmounts = src.readHashMap(mapOf<Int,Float>().javaClass.classLoader) as SortedMap<Int, Float>
            }
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun writeToParcel(dst: Parcel, flags: Int) {
            super.writeToParcel(dst, flags)
            dst.writeInt(if (ssIsOpened) 1 else 0)
            dst.writeString(ssCategory)
            dst.writeInt(ssCategoryColor)
            dst.writeList(ssDetailsCategory)
            dst.writeMap(ssAxisDates)
            dst.writeMap(ssAxisAmounts)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)
            override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
        }
    }
}