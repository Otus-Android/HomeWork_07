package otus.homework.customview

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup


class DiagramViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        require(childCount == 2)
        val cycleDiagram = getChildAt(0)
        val graph = getChildAt(1)



        //тут тоже надо учесть margine
//        val marg =  (cycleDiagram.layoutParams as LayoutParams).margine
        val marg = 0
        cycleDiagram.layout(
            l+marg, t+marg, r-marg, b-marg
        )

        val cycleWidth = (cycleDiagram.layoutParams as LayoutParams).diagramWidth2
//        val cycleWidth = 100
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
        val cycleWidth = (cycleDiagram.layoutParams as LayoutParams).diagramWidth2
        val widthOfCycleDiagramm = cycleWidth*2
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



    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    class LayoutParams(context: Context, attrs: AttributeSet) :
        ViewGroup.LayoutParams(context, attrs) {
        var margine: Int = 0
        var diagramWidth2: Int = 0

        init {

            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.DiagramViewGroup_Layout)
            diagramWidth2 =
                typedArray.getInt(R.styleable.DiagramViewGroup_Layout_layout_diagram_width, 100)
            margine = typedArray.getInt(R.styleable.DiagramViewGroup_Layout_layout_all_margin, 100)
            typedArray.recycle()

        }
    }
}

