package otus.homework.customview.pie

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import androidx.annotation.ColorInt
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import kotlinx.android.parcel.Parcelize
import otus.homework.customview.R
import otus.homework.customview.models.SpendItem
import otus.homework.customview.utils.ColorGenerator
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.*

interface PieCartCallback {
    fun sectorClick(category: String)
}

@Parcelize
data class SectorEntity(
    val percent: Float = 0f,
    val name: String = "",
    val amount: Int = 0,
    val sectorAngleSize: Float = 0f,
    val sectorCenterAngle: Float = 0f,
    val startAngle: Float = 0f,
    val endAngle: Float = 0f,
    @ColorInt val sectorColor: Int = 0
) : Parcelable {

    fun getFormattedPercent(): String = String.format("%.1f", percent) + "%"

    fun isAngleInSector(angle: Float, rotationAngle: Float): Boolean {
        val rotationStartAngle = (startAngle + rotationAngle) % 360
        val rotationEndAngle = (endAngle + rotationAngle) % 360

        return if (rotationStartAngle <= rotationEndAngle) {
            angle in rotationStartAngle..rotationEndAngle
        } else {
            angle >= rotationStartAngle && angle in 270f..360f || angle <= rotationEndAngle && angle in 0f..90f
        }
    }
}

class PieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var totalAmount: Int = 0

    private val mScaledDensity = context.resources.displayMetrics.scaledDensity

    private var callback: PieCartCallback? = null
    private val sectorItems: ArrayList<SectorEntity> = arrayListOf()
    private var selectedSector: SectorEntity? = null

    private val baseColor = ContextCompat.getColor(context, R.color.pie_baseColor)
    private val whiteColor = ContextCompat.getColor(context, R.color.white)
    private val centerTextColor = ContextCompat.getColor(context, R.color.black)
    private val sectorTextColor = ContextCompat.getColor(context, R.color.black)

    private val defaultCallbackText = context.resources.getString(R.string.total)

    private val minPieChartSize = context.resources.getDimension(R.dimen.minPieChartSize).toInt()

    private val viewRect = Rect()
    private val outerRect = RectF()
    private val innerRect = RectF()

    private var rotationAngle = 0f
    private val sectorsPath = Path()

    private var insetRatio = 0.2f

    private val centerText: String
        get() = "${(selectedSector?.amount ?: totalAmount)} $"

    private val sectorPaint = Paint(ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val sectorStrokePaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = whiteColor
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val centerTextPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = centerTextColor
        textSize = 24 * mScaledDensity
        textAlign = Paint.Align.CENTER
    }

    private val sectorTextPaint = Paint(ANTI_ALIAS_FLAG).apply {
        color = sectorTextColor
        textSize = 10 * mScaledDensity
        textAlign = Paint.Align.CENTER
    }

    private val rotateSectorTimeMs: Long = 1000L

    private val centerTextAnimationListener = AnimatorUpdateListener {
        centerTextPaint.alpha = it.animatedValue as Int
    }

    private val centerTextAnimationFadeIn =
        ValueAnimator.ofInt(0, 255).apply {
            addUpdateListener(centerTextAnimationListener)
        }

    private val centerTextAnimationFadeOut =
        ValueAnimator.ofInt(255, 0).apply {
            addUpdateListener(centerTextAnimationListener)
        }

    private val centerTextAnimationSet = AnimatorSet().apply {
        interpolator = AccelerateInterpolator()
        playSequentially(centerTextAnimationFadeOut, centerTextAnimationFadeIn)
        duration = rotateSectorTimeMs / this.childAnimations.size
    }

    private val generalGestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
                return handleViewClick(event)
            }
        })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return generalGestureDetector.onTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val chartWidth = max(minPieChartSize, suggestedMinimumWidth + paddingLeft + paddingRight)
        val chartHeight = max(minPieChartSize, suggestedMinimumHeight + paddingTop + paddingBottom)

        val size = min(
            resolveSize(chartWidth, widthMeasureSpec),
            resolveSize(chartHeight, heightMeasureSpec)
        )

        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.getClipBounds(viewRect)

        viewRect.also {
            val width = viewRect.width()
            val height = viewRect.height()

            outerRect.set(viewRect)
            innerRect.set(viewRect)

            innerRect.inset(width * insetRatio, height * insetRatio)
        }

        drawSectors(canvas)
        drawCenterText(canvas)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val savedState = SavedState(superState)

        savedState.rotatingAngle = rotationAngle
        savedState.sectorList = sectorItems
        savedState.selectedSector = selectedSector

        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        rotationAngle = state.rotatingAngle

        selectedSector = state.selectedSector

        if (sectorItems.isEmpty()) {
            sectorItems.addAll(state.sectorList)
        }

        selectedSector?.let {
            callback?.sectorClick(it.name)
        }

        invalidate()
    }

    private fun drawCenterText(canvas: Canvas) {
        val x = innerRect.centerX()
        val y = innerRect.centerY() - ((centerTextPaint.descent() + centerTextPaint.ascent()) / 2)

        canvas.drawText(centerText, x, y, centerTextPaint)
    }

    private fun drawSectors(canvas: Canvas) {
        sectorItems.forEach { item ->
            sectorPaint.color = item.sectorColor

            sectorsPath.apply {
                moveTo(innerRect.right, innerRect.centerY())
                arcTo(innerRect, item.startAngle, item.sectorAngleSize)
                arcTo(outerRect, item.endAngle, 0f)
                arcTo(outerRect, item.endAngle, item.sectorAngleSize.unaryMinus())
                arcTo(innerRect, item.startAngle, 0f)
            }

            canvas.save()
            canvas.rotate(rotationAngle, innerRect.centerX(), innerRect.centerY())
            canvas.drawPath(sectorsPath, sectorPaint)
            canvas.drawPath(sectorsPath, sectorStrokePaint)
            canvas.restore()
            sectorsPath.reset()

            val angleInRadian =
                ((item.endAngle + item.startAngle) / 2 + rotationAngle) * PI / 180

            val distance = (innerRect.width() + outerRect.width()) / 4f

            val x = distance * cos(angleInRadian).toFloat() + outerRect.centerX()
            val y = distance * sin(angleInRadian).toFloat() + outerRect.centerY() -
                    ((sectorTextPaint.descent() + sectorTextPaint.ascent()) / 2)

            canvas.drawText(item.getFormattedPercent(), x, y, sectorTextPaint)
        }
    }

    private fun handleViewClick(event: MotionEvent): Boolean {
        val innerRadius = innerRect.width() / 2f
        val outerRadius = outerRect.width() / 2f

        val zeroX = event.x - innerRect.centerX()
        val zeroY = -(event.y - innerRect.centerY())

        val eventRadius = hypot(zeroX, zeroY)

        return if (eventRadius in innerRadius..outerRadius) {
            val eventAngleRad = atan2(zeroY, zeroX).let {
                if (it < 0) abs(it) else 2 * PI - it
            }.toDouble()

            val eventAngleDegrees = Math.toDegrees(eventAngleRad)

            val clickedSector = sectorItems.find {
                it.isAngleInSector(eventAngleDegrees.toFloat(), rotationAngle)
            }

            clickedSector?.let { sector ->

                val sectorCenterAngle = (rotationAngle + sector.sectorCenterAngle) % 360

                val targetRotationAngle = if (sectorCenterAngle in 90f..270f) {
                    270 - sectorCenterAngle
                } else {
                    if (sectorCenterAngle in 270f..360f) {
                        270 - sectorCenterAngle
                    } else {
                        (90 + sectorCenterAngle).unaryMinus()
                    }
                }

                startAnimationRotation(
                    rotationAngle,
                    rotationAngle + targetRotationAngle,
                    rotateSectorTimeMs
                )

                centerTextAnimationFadeOut.doOnEnd {
                    this.selectedSector = sector
                }

                centerTextAnimationSet.start()
            }
            true
        } else {
            false
        }
    }

    fun setItems(items: List<SpendItem>) {
        totalAmount = items.fold(0) { acc, spendItem -> acc.plus(spendItem.amount) }

        val spendGroups = items.groupBy { it.category }

        var sectorAngle = 0f

        val chartPalette = ColorGenerator.generatePalette(spendGroups.size, baseColor, false)

        spendGroups.entries.forEachIndexed { index, entry ->
            val groupAmount = entry.value.fold(0) { acc, item -> acc.plus(item.amount) }
            val groupSpendPercent = groupAmount * 100f / totalAmount

            val groupStartAngle = sectorAngle
            val angleIncrement = 360 * groupSpendPercent / 100
            val groupEndAngle = groupStartAngle + angleIncrement

            sectorAngle = groupEndAngle

            val item = SectorEntity(
                name = entry.key,
                amount = groupAmount,
                percent = groupSpendPercent,
                startAngle = groupStartAngle,
                endAngle = groupEndAngle,
                sectorAngleSize = angleIncrement,
                sectorCenterAngle = groupStartAngle + angleIncrement / 2,
                sectorColor = chartPalette[index]
            )

            sectorItems.add(item)
        }

        invalidate()
    }

    fun setCallback(callback: PieCartCallback) {
        if (this.callback == null) {
            this.callback = callback
        }
    }

    fun removeCallback() {
        this.callback = null
    }

    fun startAnimationRotation(
        fromAngle: Float = 0f,
        toAngle: Float = 1080f,
        duration: Long = 2000
    ) {
        ValueAnimator.ofFloat(fromAngle, toAngle).apply {
            this.duration = duration
            interpolator = AnticipateOvershootInterpolator()

            addUpdateListener {
                rotationAngle = (animatedValue as Float) % 360
                invalidate()
            }

            doOnEnd {
                if (rotationAngle < 0) rotationAngle = (rotationAngle + 360)

                val callbackText = selectedSector?.name ?: defaultCallbackText
                this@PieChart.callback?.sectorClick(callbackText)
            }

            start()
        }
    }

    internal class SavedState : BaseSavedState {
        var rotatingAngle = 0f
        var sectorList: ArrayList<SectorEntity> = arrayListOf()
        var selectedSector: SectorEntity? = null

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            rotatingAngle = parcel.readFloat()

            selectedSector = parcel.readParcelable(SectorEntity::class.java.classLoader)

            sectorList = arrayListOf()
            parcel.readList(sectorList, List::class.java.classLoader)
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)

            out.writeFloat(rotatingAngle)
            out.writeParcelable(selectedSector, PARCELABLE_WRITE_RETURN_VALUE)
            out.writeArray(arrayOf(sectorList))
        }


        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState?> =
                object : Parcelable.Creator<SavedState?> {
                    override fun createFromParcel(parcel: Parcel): SavedState {
                        return SavedState(parcel)
                    }

                    override fun newArray(size: Int): Array<SavedState?> {
                        return arrayOfNulls(size)
                    }
                }
        }
    }
}
