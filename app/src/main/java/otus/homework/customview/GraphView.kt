package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import kotlin.collections.ArrayList

class GraphView(context: Context, attrs: AttributeSet): View(context, attrs) {

    private val widthDefault = (200 * context.resources.displayMetrics.density).toInt()
    private val heightDefault = (450 * context.resources.displayMetrics.density).toInt()

    private var heightMax = 1
    private var daysMoneySpent: ArrayList<DayMoneySpent>? = ArrayList()
    private var paint = Paint().apply {
        style = Paint.Style.STROKE
        flags = Paint.ANTI_ALIAS_FLAG
        color = Color.WHITE
        strokeWidth = 2 * context.resources.displayMetrics.density
    }
    private var path = Path()

    fun setItems(items: ArrayList<Item>) {
        this.daysMoneySpent!!.clear()
        items.sortedBy { it.time }.forEach { item ->

            var lastElement: DayMoneySpent? = null
            daysMoneySpent!!.forEach {
                if (it.date == item.time) {
                    lastElement = it
                    return@forEach
                }
            }
            if (lastElement != null) {
                lastElement!!.totalAmount = lastElement!!.totalAmount + item.amount
            } else {
                daysMoneySpent!!.add(DayMoneySpent(item.amount, item.time))
            }
        }

        heightMax = daysMoneySpent!!.maxOf { it.totalAmount}

        requestLayout()
        invalidate()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)
        val widthMeasure = MeasureSpec.getSize(widthMeasureSpec)
        val heightMeasure = MeasureSpec.getSize(heightMeasureSpec)
        val width = measureSize(modeWidth, widthMeasure, widthDefault)
        val height = measureSize(modeHeight, heightMeasure, heightDefault)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val first = daysMoneySpent!!.first()

        path.moveTo(0f, ((first.totalAmount.toDouble()/heightMax.toDouble()) * height).toFloat())

        for (index in 0 until daysMoneySpent!!.size) {
            val x = (index.toDouble()/daysMoneySpent!!.size.toDouble() * width).toFloat()
            val y = ((daysMoneySpent!!.get(index).totalAmount.toDouble()/heightMax.toDouble()) * height).toFloat()
            path.lineTo(x, y)
            if (index+1 == daysMoneySpent!!.size) {
                path.lineTo(width.toFloat(), y)
            }
        }
        canvas?.drawPath(path, paint)
    }

    private fun measureSize(mode: Int, size: Int, sizeMax: Int) = when (mode) {
        MeasureSpec.EXACTLY -> {
            size
        }
        MeasureSpec.AT_MOST -> {
            if (sizeMax <= size) {
                sizeMax
            } else {
                size
            }
        }
        MeasureSpec.UNSPECIFIED -> {
            sizeMax
        }
        else -> {
            size
        }
    }

    override fun onSaveInstanceState(): Parcelable =
        Bundle().apply {
            putInt("heightMax", heightMax)
            putParcelableArrayList("daysMoneySpent", daysMoneySpent)
            putParcelable("superState", super.onSaveInstanceState())
        }


    override fun onRestoreInstanceState(state: Parcelable?) = super.onRestoreInstanceState(
        if (state is Bundle) {
            heightMax = state.getInt("heightMax")
            daysMoneySpent = state.getParcelableArrayList("daysMoneySpent")
            state.getParcelable("superState")
        } else {
            state
        }
    )

}