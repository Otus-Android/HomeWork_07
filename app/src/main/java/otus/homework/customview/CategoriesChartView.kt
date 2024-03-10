package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import java.time.Instant
import java.time.ZoneId

class CategoriesChartView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : CustomVew(context, attrs) {

  private val categories = MutableLiveData<List<CategoryUiModel>>()
  private val dateCount = MutableLiveData<Int>()

  val path = Path()

  private val lineOrangePaint: Paint =
      Paint().apply {
        color = context.getColor(R.color.orange)
        strokeWidth = 10f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(100f)
      }

  private val linePurplePaint =
      Paint().apply {
        color = context.getColor(R.color.purple_200)
        strokeWidth = 10f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(100f)
      }
  private val lineLightBluePaint =
      Paint().apply {
        color = context.getColor(R.color.teal_200)
        strokeWidth = 10f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(100f)
      }
  private val lineDarkPurplePaint =
      Paint().apply {
        color = context.getColor(R.color.purple_700)
        strokeWidth = 10f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(100f)
      }
  private val lineOlivePaint =
      Paint().apply {
        color = context.getColor(R.color.olive)
        strokeWidth = 10f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(100f)
      }
  private val lineTealPaint =
      Paint().apply {
        color = context.getColor(R.color.teal_700)
        strokeWidth = 10f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(100f)
      }
  private val lineLimePaint =
      Paint().apply {
        color = context.getColor(R.color.lime)
        strokeWidth = 10f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(100f)
      }
  private val lineRedPaint =
      Paint().apply {
        color = context.getColor(R.color.red)
        strokeWidth = 10f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(100f)
      }

  private val lineBlackPaint =
      Paint().apply {
        color = context.getColor(R.color.black)
        strokeWidth = 20f
        style = Paint.Style.STROKE
      }

  override fun onSaveInstanceState(): Parcelable {
    super.onSaveInstanceState()
    val bundle =
        Bundle().apply {
          putParcelableArray(CATEGORIES_KEY, categories.value?.toTypedArray())
        }
    return bundle
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    if (state is Bundle) {
      state.getParcelableArrayList<CategoryUiModel>(CATEGORIES_KEY)?.let { categories.value = it }
    }
    super.onRestoreInstanceState(state)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    val categories = categories.value ?: emptyList()
    val amountRange = getMaxValue(listPayload) - getMinValue(listPayload)
    val timeRange = (dateCount.value ?: 0) + 1
    val wStep = width.toFloat() / timeRange
    val minValue = getMinValue(listPayload)

    categories.forEachIndexed { index, categoryUiModel ->
      path.reset()

      path.moveTo(START_X, height.toFloat() - START_X)

      val (firstTime, firstAmount) = categoryUiModel.valueForDate.first()
      val firstX = wStep + START_X
      val firstY = height - (firstAmount - minValue) / amountRange * height - START_X
      path.lineTo(firstX, firstY)

      categoryUiModel.valueForDate.drop(1).forEach { (time, amount) ->
        val x = wStep * time + START_X
        val y = height - (amount - minValue) / amountRange * height - START_X
        path.lineTo(x, y)
      }
      path.lineTo(width.toFloat() - 100, height.toFloat() - START_X)

      canvas.drawPath(path, getPaint(index))
    }

    canvas.drawLine(START_X, height.toFloat() - START_X, START_X, 60f, lineBlackPaint)
    canvas.drawLine(
        START_X,
        height.toFloat() - START_X,
        width.toFloat() - 40f,
        height.toFloat() - START_X,
        lineBlackPaint)
    path.reset()
    path.moveTo(20f, 140f)
    path.lineTo(START_X, 60f)
    path.lineTo(80f, 140f)
    canvas.drawPath(path, lineBlackPaint)
    path.reset()
    path.moveTo(width.toFloat() - 110f, height.toFloat() - 80f)
    path.lineTo(width.toFloat() - 40f, height.toFloat() - START_X)
    path.lineTo(width.toFloat() - 110f, height.toFloat() - 20f)
    canvas.drawPath(path, lineBlackPaint)
  }

  private fun getPaint(index: Int): Paint {
    return when (index) {
      0 -> lineOrangePaint
      1 -> linePurplePaint
      2 -> lineLightBluePaint
      3 -> lineDarkPurplePaint
      4 -> lineTealPaint
      5 -> lineLimePaint
      6 -> lineRedPaint
      else -> lineOlivePaint
    }
  }

  private fun getMaxValue(payloads: List<PayloadUiModel>): Float {
    val padding = (payloads.maxOf { it.amount } - payloads.minOf { it.amount }) * 0.1
    return (payloads.maxOf { it.amount } + padding).toFloat()
  }

  private fun getMinValue(payloads: List<PayloadUiModel>): Float {
    return (payloads.map { it.amount }.minOf { it } - 100).toFloat()
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun convertPayloadsToCategories(payloads: List<PayloadUiModel>): List<CategoryUiModel> {
    val allDates =
        payloads
            .map { payload ->
              Instant.ofEpochSecond(payload.time.toLong())
                  .atZone(ZoneId.systemDefault())
                  .toLocalDate()
            }
            .toSet()
            .sorted()

    val dateToOrdinalMap = allDates.withIndex().associate { (index, date) -> date to index + 1 }
    dateCount.value = dateToOrdinalMap.size
    return payloads
        .groupBy { it.category }
        .map { (category, payloadsInCategory) ->
          val valuesForDate =
              payloadsInCategory
                  .groupBy { payload ->
                    Instant.ofEpochSecond(payload.time.toLong())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                  }
                  .mapNotNull { (date, payloadsForDate) ->
                    dateToOrdinalMap[date]?.let { dateOrdinal ->
                      val sumAmountForDate = payloadsForDate.sumOf { it.amount }
                      Pair(dateOrdinal, sumAmountForDate)
                    }
                  }
                  .sortedBy { it.first }

          CategoryUiModel(category = category, valueForDate = valuesForDate)
        }
  }

  @RequiresApi(Build.VERSION_CODES.O)
  fun setCategories(payloads: List<PayloadUiModel>) {
    setValues(payloads)
    categories.value = convertPayloadsToCategories(payloads)
  }

  companion object {
    private const val START_X = 50f
    private const val CATEGORIES_KEY = "categories"
  }
}
