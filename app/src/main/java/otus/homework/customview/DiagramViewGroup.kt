package otus.homework.customview

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup


class DiagramViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {
    val margine = 100
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        require(childCount == 2)
        val cycleDiagram = getChildAt(0)
        val graph = getChildAt(1)

        //тут тоже надо учесть margine
        cycleDiagram.layout(
            l, t, r, b
        )

        val cycleWidth = 100
        val diff = cycleDiagram.width - 2*cycleWidth - graph.measuredWidth

        graph.layout(
            cycleDiagram.left+cycleWidth+diff/2,
            cycleDiagram.top+cycleDiagram.height/2,
            cycleDiagram.right-cycleWidth-diff/2,
            cycleDiagram.bottom-cycleWidth
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
        val widthOfCycleDiagramm = 200
        val cyclePadding = width/10
        graph.measure(
            MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize((cycleDiagram.measuredWidth-2*widthOfCycleDiagramm-cyclePadding*2)),
                MeasureSpec.EXACTLY
            ),
            MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec),
                MeasureSpec.AT_MOST
            )
        )

        setMeasuredDimension(
            cycleDiagram.measuredWidth,
            cycleDiagram.measuredHeight)

    }
}