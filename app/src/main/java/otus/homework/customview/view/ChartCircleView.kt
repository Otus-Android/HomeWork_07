package otus.homework.customview.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.text.MeasuredText
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import otus.homework.customview.R
import otus.homework.customview.model.Store
import otus.homework.customview.utils.GradientConstant
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random


private const val ONE_RADIAN = 0.0174533f
private const val ONE_DEGREE = 57.2958

class ChartCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    /*
    TODO
    1. добавить выбор вида. viewType
    2. выбор радиуса круга. radiusCircle

     */
    private val list = ArrayList<Int>()
    private val listStore = ArrayList<Store>()
    private var maxValue = 0
    private var sumValues = 0
    private lateinit var paintBaseFill: Paint
    private lateinit var paintSelect: Paint
    private lateinit var paintDangerFill: Paint
    private lateinit var paintStroke: Paint
    private lateinit var paintPath: Paint
    private lateinit var paintTitle: Paint
    private lateinit var paintWhite: Paint
    private var strokeWidthNew: Float
    private val rect = RectF()
    private var dx = 6f
    private var dy = 6f
    private val colorNew = listOf(
        Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.parseColor("#00ffc5"),
        Color.parseColor("#ff6800"), Color.parseColor("#bde619"), Color.parseColor("#ddadaf"),
        Color.parseColor("#ff7f50"), Color.parseColor("#7743eb"), Color.parseColor("#872a08"),
        Color.parseColor("#d8bfd8"),
    )

    private val animator: ValueAnimator
    private val path: Path
    private var lastSelect = 0
    private var widthHalf = 0f
    private var heightHalf = 0f
    private var title = ""
    private var viewType = true
    private var radiusCircle = 400f
    private var radiusTitle = 150f
    private var radiusText = 450f

    private val platinumBackground = GradientConstant.platinumGradient(context)
    private val goldBackground = GradientConstant.goldGradient(context)

    init {

        background = platinumBackground
        if (isInEditMode) {
            setValues(
                listOf(1500, 499, 129, 4541, 1600, 1841, 369, 100, 8000, 809, 1000, 389)
            )
            //setValues(listOf(1500, 499, 129, 4541))
            //setValues(listOf(1500, 499, 129, 4541, 1600, 1841, 369, 100, 8000))
        }

        val oneDp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 1f,
            context.resources.displayMetrics
        )

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ChartCircleView)

        strokeWidthNew =
            typeArray.getDimension(R.styleable.ChartCircleView_strokeWidth, 82.5f) / oneDp
        viewType = typeArray.getBoolean(R.styleable.ChartCircleView_viewType, true)
        radiusCircle =
            typeArray.getFloat(R.styleable.ChartCircleView_radiusCircle, 400f)
        radiusText =
            typeArray.getFloat(R.styleable.ChartCircleView_radiusText, 450f)

        typeArray.recycle()


        val newRadius = dipToPixels(context, strokeWidthNew)

        Log.d(
            "dimension",
            "$strokeWidthNew = $newRadius  ${radiusCircle / oneDp}  $radiusText 1dp = $oneDp"
        )
        setup(strokeWidthNew)


        val dm = context.resources.displayMetrics

        val width = dm.widthPixels
        val height = dm.heightPixels

        // context.resources.getDimension(R.dimen.strokeWidth)

        // Initialize path object
        path = Path()

        // Initialize animator object
        animator = ValueAnimator()
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener {
            update(it)
        }

        // Initialize click listener
        /*setOnClickListener {
         //   setChecked(!checked, !ignoreAnimation)
        }*/
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        when (wMode) {
            MeasureSpec.EXACTLY -> {
                println("ShuView: EXACTLY $wSize $hSize")
                setMeasuredDimension(wSize, hSize)
            }

            MeasureSpec.AT_MOST -> {
                println("ShuView: AT_MOST $wSize $hSize")
                //   val newW = min((list.size * barWidth).toInt(), wSize)
                //  setMeasuredDimension(newW, hSize)
            }

            MeasureSpec.UNSPECIFIED -> {
                println("ShuView: UNSPECIFIED $wSize $hSize")
                //   setMeasuredDimension((list.size * barWidth).toInt(), wSize)

            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        widthHalf = width / 2f
        heightHalf = height / 2f

        //coordinate Rect
        val left = widthHalf - radiusCircle
        val top = heightHalf - radiusCircle
        val right = widthHalf + radiusCircle
        val bottom = heightHalf + radiusCircle

        //Coordinate top text
        var topText = top - 100f
        // Check summa Angle , must 360
        var sumAngle = 0f
        //выбор цвета
        var currentColor = 0

        if (list.size == 0) return

        //Находим один градус
        val oneChunk = 360f / sumValues
        //Находим для ширины
        val oneChunkRect = 100f / sumValues

        //Начальные значения
        var currentStartAngle = 0f
        var currentSweepAngle = 0f

        rect.set(left, top, right, bottom)

        var selectColor = Color.RED

        // Cycle Draw
        for (store in listStore) {
            currentSweepAngle = store.amount * oneChunk
            sumAngle += currentSweepAngle
            paintBaseFill.color = colorNew[currentColor]
            paintSelect.color = colorNew[currentColor]

            //Задаём смещение . dx = 0f без смещения.
            dx = if (viewType) store.amount * oneChunkRect
            else 0f

            dy = dx
            rect.set(left - dx, top - dy, right + dx, bottom + dy)

            //Draw Metka . 35f and 10f выравнивание текста.
            val angleForText = (currentStartAngle + (currentSweepAngle / 2f)) * ONE_RADIAN
            val x = widthHalf - 35f + cos(angleForText) * radiusText
            val y = heightHalf + 10f + sin(angleForText) * radiusText

            //Draw Arc
            if (store.isSelect) {
                selectColor = colorNew[currentColor]
                canvas.drawArc(
                    rect,
                    currentStartAngle,
                    currentSweepAngle,
                    true,
                    paintSelect
                )
                title = store.category
                canvas.drawText(
                    "${store.amount} ", x, y, paintTitle
                )
            } else {
                canvas.drawArc(rect, currentStartAngle, currentSweepAngle, true, paintBaseFill)
                canvas.drawText(
                    "${store.amount} ", x, y, paintStroke
                )
            }

            rect.set(left, top, right, bottom)

            //можно сделать флажок для вычисления один раз.
            store.beginDegree = currentStartAngle.toInt()
            //Next Angle
            currentStartAngle += currentSweepAngle
            store.endDegree = currentStartAngle.toInt()

            //Check last color in ColorNew
            if (currentColor == colorNew.size - 1) {
                currentColor = 0
            } else currentColor += 1

        }
        //Draw White Circle
        // And Change Gradient in center

        val gradient = RadialGradient(
            widthHalf,
            heightHalf,
            radiusCircle - strokeWidthNew * 2,
            intArrayOf(Color.WHITE, selectColor),
            null,
            Shader.TileMode.CLAMP
        )

        paintWhite.shader = gradient
        canvas.drawCircle(widthHalf, heightHalf, radiusCircle - strokeWidthNew * 2, paintWhite)


        //Draw animated Text

        path.reset()
        // Draw borders
        path.arcTo(
            left + radiusTitle ,
            top + radiusTitle,
            right - radiusTitle,
            bottom - radiusTitle,
            180f ,
            180f ,
            false
        )

        val halfText = 180f
        val countChar = title.count()
        val sweepText = halfText - countChar * 10

        canvas.drawTextOnPath(
            title,
            path,
            sweepText,
            0f,
            paintTitle
        )

        canvas.drawPath(path, paintPath)

    }

    private fun startAnimation() {
        animator.setFloatValues(300f, 150f)
        animator.duration = 1000L
        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationCancel(animation: Animator) {
                animation.removeAllListeners()
            }

            override fun onAnimationEnd(animation: Animator) {
            }
        })
        animator.start()
    }
    private fun update(animation: ValueAnimator) {
        radiusTitle = animation.animatedValue as Float
        invalidate()
    }


    private var lastTouchX = 0f
    private var lastTouchY = 0f
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            lastTouchX = event.x
            lastTouchY = event.y
            searchSelect()
            startAnimation()
            invalidate()
        }
        return true
    }

    private fun searchSelect() {
        //начальные координаты A (widthHalf, heightHalf) B (widthHalf + 400, heightHalf) AB (400, 0)
        // C (lastTouchX, lastTouchY) AC ( lastTouchX - widthHalf, lastTouchY - heightHalf)
        // координаты выбраной точки
        val x1 = 400
        val y1 = 0
        val x2 = lastTouchX - widthHalf
        val y2 = lastTouchY - heightHalf

        val angle1 = x1 * x2 + y1 * y2
        val angle2 = sqrt((x1 * x1).toDouble() + (y1 * y1))
        val angle3 = sqrt((x2 * x2).toDouble() + (y2 * y2))
        val angle4 = angle2 * angle3
        var angle5 = acos(angle1 / angle4) * ONE_DEGREE
        if (y2 < 0) {
            angle5 = 180 + 180 - angle5
        }
        // Search select category
        listStore[lastSelect].isSelect = false
        listStore.forEachIndexed { index, store ->
            if (angle5 > store.beginDegree && angle5 < store.endDegree) {
                store.isSelect = true
                onItemClickListener?.let { it(index) }
                lastSelect = index
            }
        }
    }

    fun setValues(values: List<Int>, listStoreNew: List<Store> = emptyList()) {
        list.clear()
        list.addAll(values)
        listStore.clear()
        listStore.addAll(listStoreNew)
        maxValue = list.max()
        sumValues = list.sum()
        requestLayout()
        invalidate()
    }

    private fun setup(strokeWidthNew: Float) {
        paintBaseFill = Paint().apply {
            color = Color.GREEN
            strokeWidth = strokeWidthNew
            style = Paint.Style.FILL
        }
        paintSelect = Paint().apply {
            color = Color.GREEN
            strokeWidth = strokeWidthNew
            style = Paint.Style.FILL_AND_STROKE
        }

        paintDangerFill = Paint().apply {
            color = Color.RED
            strokeWidth = strokeWidthNew
            style = Paint.Style.STROKE
        }
        paintStroke = Paint().apply {
            color = Color.BLACK
            textSize = 40f
            style = Paint.Style.FILL
            strokeWidth = 2.0f
        }

        paintPath = Paint().apply {
            color = Color.TRANSPARENT
            textSize = 40f
            style = Paint.Style.STROKE
            strokeWidth = 2.0f
        }

        paintTitle = Paint().apply {
            color = Color.BLUE
            style = Paint.Style.FILL_AND_STROKE
            textSize = 70f
            strokeWidth = 2.0f
        }
        paintWhite = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
    }

    private fun randomColor(): Int {
        return Color.HSVToColor(floatArrayOf(Random.nextInt(361).toFloat(), 1f, 1f))
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>?) {
        super.dispatchSaveInstanceState(container)
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

    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    private fun dipToPixels(context: Context, dipValue: Float): Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics)
    }

    private fun width() = width
    private fun height() = height
    private fun centerX() = width / 2f
    private fun centerY() = height / 2f
    private fun radius() = (if (width() > height()) height() else width()) / 2f
    private fun radiusBorderChecked() = (if (width() > height()) height() else width()) / 2f

}

