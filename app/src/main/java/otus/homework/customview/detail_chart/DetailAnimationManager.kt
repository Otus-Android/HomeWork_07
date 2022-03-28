package otus.homework.customview.detail_chart

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.DashPathEffect
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.PathMeasure

class DetailAnimationManager(
    private val listener: AnimationListener
) {

    interface AnimationListener {
        fun onAnimateChart(pathEffect: PathEffect)
        fun onAnimateDots(alpha: Int)
    }

    fun startAnimate(chartPath: Path) {
        val measure = PathMeasure(chartPath, false)
        val chartLength = measure.length

        val pathAnimator = ValueAnimator.ofFloat(1.0f, 0.0f).apply {
            duration = 500
            addUpdateListener {
                listener.onAnimateChart(createPathEffect(chartLength, it.animatedValue as Float))
            }
        }
        val circleAnimator = ValueAnimator.ofInt(0, 255).apply {
            duration = 200
            addUpdateListener {
                listener.onAnimateDots(it.animatedValue as Int)
            }
        }
        val set = AnimatorSet()
        set.playSequentially(pathAnimator, circleAnimator)
        set.start()
    }

    private fun createPathEffect(pathLength: Float, phase: Float): PathEffect {
        return DashPathEffect(
            floatArrayOf(pathLength, pathLength),
            (phase * pathLength)
        )
    }

}