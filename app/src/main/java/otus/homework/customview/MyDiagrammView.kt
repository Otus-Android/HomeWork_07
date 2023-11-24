package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class MyDiagrammView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null
    ): View(context, attrs) {

    private val values = ArrayList<Float>()

    private var paddingByHeight = 0f
    private val padding = 100f

    private lateinit var paintBackground : Paint
    private lateinit var paintMyRed: Paint
    private lateinit var paintMyOrange: Paint
    private lateinit var paintMySand: Paint
    private lateinit var paintMyPeach: Paint
    private lateinit var paintMyLemon: Paint
    private lateinit var paintMyLime: Paint
    private lateinit var paintMyWave: Paint
    private lateinit var paintMyOcean: Paint
    private lateinit var paintMyNight: Paint
    private lateinit var paintMyDeep: Paint

    private val listOfPaints = mutableListOf<Paint>()

    private var onePercent: Float = 0.0f



    init {
        if (isInEditMode) {
            setValues(listOf(4f, 2f, 1f, 5f, 0f, 2f))
        }
        setup(
            context
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.EXACTLY -> {
                println("$TAG EXACTLY $wSize $hSize")
                paddingByHeight = (hSize - wSize).toFloat()/2
                setMeasuredDimension(wSize, hSize)
            }
            MeasureSpec.AT_MOST -> {
                println("$TAG AT_MOST $wSize $hSize")
                val barWidth = 1f
                val newW = Integer.min((values.size * barWidth).toInt(), wSize)
                setMeasuredDimension(newW, hSize)
            }
            MeasureSpec.UNSPECIFIED -> {
                val barWidth = 1f
                println("$TAG UNSPECIFIED $wSize $hSize")
                setMeasuredDimension((values.size * barWidth).toInt(), hSize)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

       val worldHeight = height.toFloat()/2
       val worldWidth = width.toFloat()/2

        canvas.drawRGB(255, 255, 255)

        if (values.size == 0) return

        var startAngle = -45f
        var paint = listOfPaints[0]

        var paintIndx = 0
        for (item in values){
            canvas.drawArc(
                  padding,
                padding + paddingByHeight,
                width.toFloat()- padding,
                height.toFloat() - paddingByHeight- padding,
                startAngle,
                (item/onePercent)*3.6f,
                true,
                paint
            )

            startAngle +=(item/onePercent)*3.6f
            paint = listOfPaints[paintIndx]
            if (paintIndx == listOfPaints.lastIndex){
                paintIndx = 0
            }else{
                paintIndx++
            }
        }

        canvas.drawOval(worldWidth-300f,worldHeight+300f,worldWidth+300f,worldHeight-300f,paintBackground)
    }

    fun setValues(values : List<Float>) {
        this.values.clear()
        this.values.addAll(values)
        val hundredPercent = this.values.sum()
        onePercent = hundredPercent/100
        requestLayout()
        invalidate()
    }

    private fun setup(
        context: Context
    ) {
        paintBackground = Paint().apply {
            color = context.getColor(R.color.white)
            style = Paint.Style.FILL
        }
        paintMyRed=Paint().apply {
              color = context.getColor(R.color.my_red)
            style = Paint.Style.FILL
        }
        paintMyOrange=Paint().apply {
              color = context.getColor(R.color.my_orange)
            style = Paint.Style.FILL
        }
        paintMySand=Paint().apply {
              color = context.getColor(R.color.my_sand)
            style = Paint.Style.FILL
        }
        paintMyPeach=Paint().apply {
              color = context.getColor(R.color.my_peach)
            style = Paint.Style.FILL
        }
        paintMyLemon=Paint().apply {
              color = context.getColor(R.color.my_lemon)
            style = Paint.Style.FILL
        }
        paintMyLime=Paint().apply {
              color = context.getColor(R.color.my_lime)
            style = Paint.Style.FILL
        }
        paintMyWave=Paint().apply {
              color = context.getColor(R.color.my_wave)
            style = Paint.Style.FILL
        }
        paintMyOcean=Paint().apply {
              color = context.getColor(R.color.my_ocean)
            style = Paint.Style.FILL
        }
        paintMyNight=Paint().apply {
              color = context.getColor(R.color.my_night)
            style = Paint.Style.FILL
        }
        paintMyDeep=Paint().apply {
              color = context.getColor(R.color.my_deep)
            style = Paint.Style.FILL
        }

        listOfPaints.run {
            add(paintMyRed)
            add(paintMyOrange)
            add(paintMySand)
            add(paintMyPeach)
            add(paintMyLemon)
            add(paintMyLime)
            add(paintMyWave)
            add(paintMyOcean)
            add(paintMyNight)
            add(paintMyDeep)
        }
    }
}