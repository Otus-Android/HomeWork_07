package otus.homework.customview.pieChart

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator

typealias InvalidateAnimatorCallback = (HashMap<String, Float>) -> Unit

class ChartAnimator(
    private val invalidateAnimatorCallback: InvalidateAnimatorCallback
) {

    companion object {
        const val strokeKeyInc = "stroke_increase"
        const val strokeKeyDec = "stroke_decrease"
        const val angleKeyInc = "angle_increase"
        const val angleKeyDec = "angle_decrease"
    }

    fun isNotRunning(): Boolean {
        return !valueAnimator.isRunning
    }

    fun startAnimation() {
        if (isNotRunning()) {
            valueAnimator.start()
        }
    }

    private val animationDuration = 500L
    private val animationInterpolator = LinearInterpolator()

    // значения которые будем использовать для указания анимации ширины сектора
    private val strokeStartAnimValue = 0.2f
    private val strokeEndAnimValue = 0.25f

    // значения которые будем испльзовать для указания градусов сектора
    private val angleStartValue = 0f
    private val angleEndValue = 6f


    private val strokeHolderInc =
        PropertyValuesHolder.ofFloat(strokeKeyInc, strokeStartAnimValue, strokeEndAnimValue)

    private val strokeHolderDec =
        PropertyValuesHolder.ofFloat(strokeKeyDec, strokeEndAnimValue, strokeStartAnimValue)

    private val angleHolderInc =
        PropertyValuesHolder.ofFloat(angleKeyInc, angleStartValue, angleEndValue)

    private val angleHolderDec =
        PropertyValuesHolder.ofFloat(angleKeyDec, angleEndValue, angleStartValue)

    private val animationResult = HashMap<String, Float>()

    private var valueAnimator: ValueAnimator =
        ValueAnimator.ofPropertyValuesHolder(
            strokeHolderInc,
            strokeHolderDec,
            angleHolderInc,
            angleHolderDec
        ).apply {

            interpolator = animationInterpolator
            duration = animationDuration

            addUpdateListener {

                animationResult[strokeKeyInc] = it.getAnimatedValue(strokeKeyInc) as Float
                animationResult[strokeKeyDec] = it.getAnimatedValue(strokeKeyDec) as Float
                animationResult[angleKeyInc] = it.getAnimatedValue(angleKeyInc) as Float
                animationResult[angleKeyDec] = it.getAnimatedValue(angleKeyDec) as Float

                invalidateAnimatorCallback.invoke(animationResult)
            }
        }

}