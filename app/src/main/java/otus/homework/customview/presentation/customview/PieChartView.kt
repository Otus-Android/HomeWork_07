package otus.homework.customview.presentation.customview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import otus.homework.customview.Event
import otus.homework.customview.models.ProductPresentationModel
import otus.homework.customview.presentation.customview.CustomViewUtils.dpToPx
import ru.otus.daggerhomework.R
import java.lang.Integer.min
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Кастомная View, которая рисует круглый график и разделяет его на кусочки (на категории)
 *
 * @author Евтушенко Максим 26.11.2023
 */
class PieChartView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null
) : View(context, attr) {

    private var onProductClickListener: ((event: Event) -> Unit)? = null

    private val desiredHeight = context.dpToPx(300).toInt()
    private val desiredWidth = context.dpToPx(300).toInt()

    private val productsMap: MutableMap<Paint, ProductPresentationModel> = mutableMapOf()
    private lateinit var defaultPaint: Paint
    private val circleRect = RectF()
    private var circleStrokeWidth: Float = context.dpToPx(50)
    private var circlePadding: Float = context.dpToPx(8)
    private var circleRadius: Float = 0F
    private var circleInnerRadius: Float = 0F
    private var circleCenterX: Float = 0F
    private var circleCenterY: Float = 0F
    private var animationSweepAngle: Int = 0

    init {
        if (isInEditMode) {
            setValues(
                listOf(
                    ProductPresentationModel(
                        percentRatio = 78f,
                        previousPercentRation = 0f,
                        lineColor = Color.RED,
                        stroke = 40,
                        event = Event.ProductClick("Пятерочка")
                    ),
                    ProductPresentationModel(
                        percentRatio = 20f,
                        previousPercentRation = 78f,
                        lineColor = Color.BLUE,
                        stroke = 40,
                        event = Event.ProductClick("Перекресток")
                    )
                )
            )
            startAnimation()
        }

        setup()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize // Задан конкретный размер для ширины
            MeasureSpec.AT_MOST -> min(desiredWidth, widthSize) // Размер не должен превышать заданный размер
            else -> desiredWidth // Задать предпочтительный размер, если точного или максимального размера не задано
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize // Задан конкретный размер для высоты
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize) // Размер не должен превышать заданный размер
            else -> desiredHeight // Задать предпочтительный размер, если точного или максимального размера не задано
        }
        calculateCircleRadius(width, height)

        setMeasuredDimension(width, height) // Устанавливаем фактический размер View
    }

    override fun onSaveInstanceState(): Parcelable? {
        // Здесь можно реализовать сохранение состояние. Но мне кажется,
        // более правильно сохранять их во вью модели
        return super.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
    }

    /**
     * Запуск анимации отрисовки View.
     */
    fun startAnimation() {
        // Проход значений от 0 до 360 (целый круг), с длительностью - 1.5 секунды
        val animator = ValueAnimator.ofInt(0, 360).apply {
            duration = 10500 // длительность анимации в миллисекундах
            interpolator = FastOutSlowInInterpolator() // интерпретатор анимации
            addUpdateListener { valueAnimator ->
                // Обновляем значение для отрисовки диаграммы
                animationSweepAngle = valueAnimator.animatedValue as Int
                // Принудительная перерисовка
                invalidate()
            }
        }
        animator.start()
    }

    fun setValues(products: List<ProductPresentationModel>) {
        productsMap.clear()
        products.forEach {
            val paint = it.createPaint(context)
            productsMap[paint] = it
        }
    }

    fun setOnClickListener(onClickListener: (event: Event) -> Unit) {
        onProductClickListener = onClickListener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, defaultPaint)
        drawCircle(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                getTouchPieChartPercent(event.x, event.y)?.let { percent ->

                    for (entry in productsMap.entries) {
                        val product = entry.value
                        if (percent > product.percentOfStartPosition
                            && percent <= product.percentOfCircle + product.percentOfStartPosition
                        ) {
                            onProductClickListener?.invoke(product.event)
                            break
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                return true
            }
        }


        return true
    }

    /**
     * Метод расчёта радиуса круговой диаграммы, установка координат для отрисовки.
     */
    private fun calculateCircleRadius(width: Int, height: Int) {
        // Рассчитываем ширину, которую будет занимать диаграмма
        val innerCirclePadding = circlePadding * 2 + circleStrokeWidth
        val circleViewWidth = (width - innerCirclePadding)

        circleRadius = if (circleViewWidth > height) {
            (height.toFloat() - circlePadding) / 2
        } else {
            circleViewWidth / 2
        }

        circleInnerRadius = circleRadius - circleStrokeWidth

        // Установка расположения круговой диаграммы на View
        with(circleRect) {
            left = circlePadding + circleStrokeWidth / 2f
            top = circlePadding + circleStrokeWidth / 2f
            right = width - circlePadding - circleStrokeWidth / 2f
            bottom = height - circlePadding - circleStrokeWidth / 2f
        }

        // Координаты центра круговой диаграммы
        circleCenterX = width / 2f
        circleCenterY = height / 2f
    }

    // Требует доработки
    private fun getTouchPieChartPercent(touchedX: Float, touchedY: Float): Float? {
        // Определяем расстояние точки от начала координат (в нашем случае от центра круга)
        // с помощью уравнения окружности (X-Xсenter)² + (Y-Ycenter)² = R²
        val squaredX = (touchedX - circleCenterX).pow(2)
        val squaredY = (touchedY - circleCenterY).pow(2)
        val currentRadius = sqrt(squaredX + squaredY)

        return if (currentRadius < circleInnerRadius || currentRadius > circleRadius) {
            null
        } else {
            calculateAngle(touchedY, touchedX)
        }
    }

    private fun calculateAngle(touchedY: Float, touchedX: Float): Float {
        var percent = Math.toDegrees(
            atan2(
                (touchedY - circleCenterY).toDouble(),
                (touchedX - circleCenterX).toDouble()
            )
        ).toFloat()
        if (percent < 0.0) {
            percent += 360f
        }
        return percent
    }

    /**
     * Метод отрисовки круговой диаграммы на Canvas.
     */
    private fun drawCircle(canvas: Canvas) {
        // Проходимся по дугам круга
        productsMap.forEach { (paint: Paint, product: ProductPresentationModel) ->
            // Если процент дуги попадает под угол отрисовки (animationSweepAngle)
            // Отображаем эту дугу на Canvas
            val startAt = product.percentOfStartPosition
            val circlePart = product.percentOfCircle
            if (animationSweepAngle > startAt + circlePart) {
                canvas.drawArc(circleRect, startAt, circlePart, false, paint)
            } else if (animationSweepAngle > startAt) {
                canvas.drawArc(
                    circleRect,
                    startAt,
                    animationSweepAngle - startAt,
                    false,
                    paint
                )
            }
        }
    }

    private fun setup() {
        defaultPaint = Paint().apply {
            color = context.getColor(R.color.pie_chart_default_color)
            style = Paint.Style.STROKE
            strokeWidth = circleStrokeWidth
        }
    }
}