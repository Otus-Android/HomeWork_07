package otus.homework.customview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import otus.homework.customview.model.Store
import otus.homework.customview.utils.GradientConstant
import otus.homework.customview.utils.TAG
import kotlin.math.max
import kotlin.math.min

class StockChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val listStore = ArrayList<Store>()
    private val listAmount = ArrayList<Int>()
    private var maxValue = 0
    private var minValue = 0
    private var lastSelect = 0

    private val paint = Paint().apply {
        color = Color.parseColor("#bd7ebe")
        strokeWidth = 8f
        style = Paint.Style.STROKE
        pathEffect = CornerPathEffect(60f)
        textSize = 30f
    }

    private val redPaint = Paint().apply {
        color = Color.parseColor("#b30000")
        style = Paint.Style.FILL
        strokeWidth = 16f
        textSize = 30f
    }

    private val selectPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        strokeWidth = 16f
        textSize = 60f
    }

    private val path = Path()
    private val upBarsPath = Path()
    private val downBarsPath = Path()

    private var scale = 1f

    private val goldBackground = GradientConstant.goldGradient(context)
    private val platinumBackground = GradientConstant.platinumGradient(context)

    private var isChangeBackground = false

    init {
        background = goldBackground
    }


    private val scaleGestureDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scale *= detector.scaleFactor
                invalidate()
                return true
            }
        })

    private val transitionDrawableGold : TransitionDrawable = TransitionDrawable(arrayOf(platinumBackground, goldBackground))
    private val transitionDrawablePlatinum : TransitionDrawable = TransitionDrawable(arrayOf(goldBackground, platinumBackground))

    override fun onDraw(canvas: Canvas) {
        val wStep = width.toFloat() / listAmount.size.toFloat()

        val hStep: Float = measuredHeight.toFloat() / (maxValue - minValue).toFloat()

        val newSize = min(listAmount.size, (listAmount.size / scale).toInt())
        val first = max(0, (listAmount.size - newSize) / 2)
        //  val wStep = measuredWidth.toFloat() / newSize.toFloat()

        path.reset()
        path.moveTo(0f, height.toFloat())

        var x = 30f
        var y = 300f
        //var stepText = 100f
        //будем рисовать разное количество точек . при увеличении и уменьшении масштаба.
        listAmount.forEachIndexed { index, item ->

            y = height - ((item - minValue) * hStep)

            path.lineTo(x, y)

            if (lastSelect == index && listStore[index].isSelect) {
                canvas.drawText("${listStore[index].amount}", x, y, selectPaint)
            } else
                canvas.drawText("${listStore[index].amount}", x, y, redPaint)

            x += wStep


            /*   canvas.drawText("График $item , x = $x  y = $y , wStep $wStep width $width, ${listAmount.size}", 10f, stepText, redPaint)
               stepText += 30f*/
        }
        path.lineTo(width.toFloat(), height.toFloat())
        path.close()
        canvas.drawPath(path, paint)
    }

    fun setValues(values: List<Int>, listStoreNew: List<Store> = emptyList()) {
        listAmount.clear()
        listAmount.addAll(values)
        listStore.clear()
        listStore.addAll(listStoreNew)
        maxValue = listAmount.max()
        minValue = listAmount.min()
        requestLayout()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        return true
    }

    fun setIndex(index: Int) {
        listStore[index].isSelect = true
        lastSelect = index
        setPageBackground()
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable {
        Log.i("Normal", "onSaveInstanceState")
        return CustomViewSavedState(super.onSaveInstanceState()).apply {
            lastIndex = lastSelect
        }

    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        Log.i("Normal", "onRestoreInstanceState")
        if (state is CustomViewSavedState) {
            super.onRestoreInstanceState(state.superState)
            lastSelect = state.lastIndex
            listStore[lastSelect].isSelect = true
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    /* override fun addChildrenForAccessibility(outChildren: java.util.ArrayList<View>?) {
        super.addChildrenForAccessibility(outChildren)
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) {
        super.dispatchSaveInstanceState(container)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun addExtraDataToAccessibilityNodeInfo(
        info: AccessibilityNodeInfo,
        extraDataKey: String,
        arguments: Bundle?
    ) {
        super.addExtraDataToAccessibilityNodeInfo(info, extraDataKey, arguments)
    }

    override fun addOnAttachStateChangeListener(listener: OnAttachStateChangeListener?) {
        super.addOnAttachStateChangeListener(listener)
    }

    override fun setOnFocusChangeListener(l: OnFocusChangeListener?) {
        Log.d("focus", "tag ${l?.TAG} hashCode ${l.hashCode()}   ")
        super.setOnFocusChangeListener(l)
    }*/

    private fun setPageBackground() {

        if (isChangeBackground) {
            transitionDrawableGold.isCrossFadeEnabled = true
            background = transitionDrawableGold
            transitionDrawableGold.startTransition(400)
            isChangeBackground = false
        } else {
            transitionDrawablePlatinum.isCrossFadeEnabled = true
            background = transitionDrawablePlatinum
            transitionDrawablePlatinum.startTransition(400)
            isChangeBackground = true
        }
    }

}