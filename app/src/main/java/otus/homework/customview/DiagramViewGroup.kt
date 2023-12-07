package otus.homework.customview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup


class DiagramViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        require(childCount == 2)
        val ring = getChildAt(0)
        val bars = getChildAt(1)

        val margin = (ring.layoutParams as LayoutParams).margin

        ring.layout(
            l + margin, t + margin, r - margin, b - margin
        )
        val ringWidth = (width.dp / 3)
        log("onLayout()__ margin = $margin,   ringWidth = $ringWidth")

        val ringShelf = ringWidth / 10
        val ringPadding = ringWidth / 6
        val d = ring.measuredWidth - 2 * ringPadding - 2 * ringShelf - 2 * ringWidth

        bars.layout(
            margin + ringPadding + ringWidth + d / 4,
            ring.top + ring.height / 2,
            r - margin - ringPadding - ringWidth - d / 4,
            ring.bottom - ringWidth
        )

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        require(childCount == 2)
        val cycleDiagram = getChildAt(0)
        val graph = getChildAt(1)

        cycleDiagram.measure(
            MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.AT_MOST
            ),
            MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec),
                MeasureSpec.AT_MOST
            )
        )

        val ringWidth = width.dp / 3
        val cyclePadding = width / 10
        graph.measure(
            MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize((cycleDiagram.measuredWidth - 2 * ringWidth - cyclePadding * 2)),
                MeasureSpec.EXACTLY
            ),
            MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec),
                MeasureSpec.AT_MOST
            )
        )
        setMeasuredDimension(
            cycleDiagram.measuredWidth,
            cycleDiagram.measuredHeight
        )
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    class LayoutParams(context: Context, attrs: AttributeSet) :
        ViewGroup.LayoutParams(context, attrs) {
        var margin: Int = 0

        init {
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.DiagramViewGroup_Layout)
            margin = typedArray.getDimensionPixelSize(
                R.styleable.DiagramViewGroup_Layout_layout_all_margin,
                100.dp
            )
            typedArray.recycle()

        }
    }

    private fun log(text: String) {
        Log.d(TAG, text)
    }
}

