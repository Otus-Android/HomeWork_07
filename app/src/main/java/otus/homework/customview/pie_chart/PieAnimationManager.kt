package otus.homework.customview.pie_chart

import android.animation.ValueAnimator
import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import otus.homework.customview.pie_chart.model.PieChartModel

class PieAnimationManager(
    private val chartModel: PieChartModel,
    private val animationListener: AnimationListener
) {

    interface AnimationListener {
        fun onIncreaseAnimationUpdate(rectF: RectF)
        fun onDecreaseAnimationUpdate(rectF: RectF)
    }

    private var rectFIncreased: RectF = RectF(0f, 0f, 100f, 100f)
    private var rectFDecreased: RectF = RectF(0f, 0f, 100f, 100f)

    fun animationIncrease(mainRectF: RectF) {
        ValueAnimator.ofFloat(0f, chartModel.animateTo).apply {
            duration = 200
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                rectFDecreased.apply {
                    left = mainRectF.left - it.animatedValue as Float
                    top = mainRectF.top - it.animatedValue as Float
                    right = mainRectF.right + it.animatedValue as Float
                    bottom = mainRectF.bottom + it.animatedValue as Float
                }
                animationListener.onIncreaseAnimationUpdate(rectFDecreased)
            }
            start()
        }
    }

    fun animationDecrease(mainRectF: RectF) {
        ValueAnimator.ofFloat(0f, chartModel.animateTo).apply {
            duration = 200
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                rectFIncreased.apply {
                    left = mainRectF.left - chartModel.animateTo + it.animatedValue as Float
                    top = mainRectF.top - chartModel.animateTo + it.animatedValue as Float
                    right = mainRectF.right + chartModel.animateTo - it.animatedValue as Float
                    bottom = mainRectF.bottom + chartModel.animateTo - it.animatedValue as Float
                }
                animationListener.onDecreaseAnimationUpdate(rectFIncreased)
            }
            start()
        }
    }

}