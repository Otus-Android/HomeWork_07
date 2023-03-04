package otus.homework.customview

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import androidx.core.animation.addListener
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import otus.homework.customview.ClickAnimationConstants.CLICK_ANIMATION_DAMPING_RATIO
import otus.homework.customview.ClickAnimationConstants.CLICK_ANIMATION_INITIAL_VELOCITY
import otus.homework.customview.ClickAnimationConstants.CLICK_ANIMATION_STIFFNESS
import otus.homework.customview.ClickAnimationConstants.CLICK_TIME_THRESHOLD
import otus.homework.customview.MotionEventExtensions.onClick
import otus.homework.customview.MotionEventExtensions.onPressDown
import otus.homework.customview.MotionEventExtensions.onRelease

/**
 * Анимация клика по кнопке лайка/дизлайка у сториса
 */
class FeedbackClickAnimator(
    view: View,
    private val isFeedbackChanged: () -> Boolean,
    private val onClick: () -> Unit,
) {
    private companion object {
        const val CLICK_TIME_THRESHOLD = 500L
        const val PRESS_DOWN_SCALE_FACTOR = 0.97f
        const val PRESS_DOWN_DURATION = 200L
    }

    private val pressDownScaleAnimator = createPressDownScaleAnimator(view, PRESS_DOWN_SCALE_FACTOR, PRESS_DOWN_DURATION)
    private val buttonClickAnimation = ClickAnimation(view)
    private val feedbackClickAnimation = FeedbackClickAnimation(view)

    fun animateOnTouch(event: MotionEvent?) {
        event?.onPressDown {
            pressDownScaleAnimator.start()
        }?.onClick(CLICK_TIME_THRESHOLD) {
            pressDownScaleAnimator.cancel()
            if (isFeedbackChanged()) {
                feedbackClickAnimation.start()
                onClick()
            } else {
                buttonClickAnimation.start()
            }
        }?.onRelease(CLICK_TIME_THRESHOLD) {
            pressDownScaleAnimator.cancel()
            buttonClickAnimation.start()
        }
    }

    private fun createPressDownScaleAnimator(
        view: View,
        scaleFactor: Float,
        duration: Long? = null,
        interpolator: Interpolator? = null,
    ): ObjectAnimator {
        return ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, scaleFactor),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, scaleFactor),
        ).apply {
            duration?.run(::setDuration)
            interpolator?.run(::setInterpolator)
        }
    }
}

class FeedbackClickAnimation(view: View) {

    private companion object FeedbackClickAnimationConstants {
        const val FIRST_SCALE_ROTATION_DURATION = 140L
        const val FIRST_ANIMATION_SCALE_FACTOR = 0.86f
        const val FIRST_ANIMATION_ROTATION = 15f
        const val FIRST_DELAY_BETWEEN_ANIMATIONS = 70L
        const val SECOND_SCALE_ROTATION_DURATION = 70L
        const val SECOND_ANIMATION_SCALE_FACTOR = 1.18f
        const val SECOND_ANIMATION_ROTATION = -30f
        const val SECOND_DELAY_BETWEEN_ANIMATIONS = 40L
        const val ANIMATION_SPRING_DAMPING_RATIO = 0.3f
        const val ANIMATION_SPRING_STIFFNESS = 500f
    }

    private val firstAnimator = createViewScaleRotationAnimator(
        view = view,
        scaleFactor = FIRST_ANIMATION_SCALE_FACTOR,
        rotation = FIRST_ANIMATION_ROTATION,
        duration = FIRST_SCALE_ROTATION_DURATION,
    )

    private val secondAnimator = createViewScaleRotationAnimator(
        view = view,
        scaleFactor = SECOND_ANIMATION_SCALE_FACTOR,
        rotation = SECOND_ANIMATION_ROTATION,
        duration = SECOND_SCALE_ROTATION_DURATION,
    )

    private val springForce = SpringForce().apply {
        finalPosition = 1f
        dampingRatio = ANIMATION_SPRING_DAMPING_RATIO
        stiffness = ANIMATION_SPRING_STIFFNESS
    }
    private val springAnimations = listOf(
        SpringAnimation(view, SpringAnimation.SCALE_X).addSpringForce(),
        SpringAnimation(view, SpringAnimation.SCALE_Y).addSpringForce(),
        SpringAnimation(view, SpringAnimation.ROTATION).addSpringForce(),
    )

    private val animatorSet = AnimatorSet().apply {
        play(secondAnimator)
            .before(SECOND_DELAY_BETWEEN_ANIMATIONS)
            .after(FIRST_DELAY_BETWEEN_ANIMATIONS)
            .after(firstAnimator)
        addListener(
            onEnd = {
                springAnimations.forEach { it.start() }
            }
        )
    }

    fun start() {
        animatorSet.start()
    }

    private fun createViewScaleRotationAnimator(
        view: View,
        scaleFactor: Float,
        rotation: Float,
        duration: Long? = null,
        interpolator: Interpolator? = null,
    ): ObjectAnimator {
        return ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, scaleFactor),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, scaleFactor),
            PropertyValuesHolder.ofFloat(View.ROTATION, rotation),
        ).apply {
            duration?.run(::setDuration)
            interpolator?.run(::setInterpolator)
        }
    }

    private fun SpringAnimation.addSpringForce() =
        apply { spring = springForce }

    private fun AnimatorSet.Builder.before(delay: Long): AnimatorSet.Builder {
        val anim = ValueAnimator.ofFloat(0f, 1f)
        anim.duration = delay
        before(anim)
        return this
    }
}

class ClickAnimation(
    view: View,
    private val startVelocity: Float = CLICK_ANIMATION_INITIAL_VELOCITY
) {
    private val clickScaleX = SpringAnimation(view, SpringAnimation.SCALE_X)
    private val clickScaleY = SpringAnimation(view, SpringAnimation.SCALE_Y)

    private val clickAnimationSpringForce = SpringForce().apply {
        finalPosition = 1f
        stiffness = CLICK_ANIMATION_STIFFNESS
        dampingRatio = CLICK_ANIMATION_DAMPING_RATIO
    }

    fun start() {
        clickScaleX.addClickSpringForce().start()
        clickScaleY.addClickSpringForce().start()
    }

    private fun SpringAnimation.addClickSpringForce() =
        apply {
            setStartVelocity(startVelocity)
            spring = clickAnimationSpringForce
        }
}

object MotionEventExtensions {

    inline fun MotionEvent.onPressDown(callback: () -> Unit): MotionEvent {
        when (actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                callback.invoke()
            }
        }
        return this
    }

    inline fun MotionEvent.onClick(
        clickTimeThreshold: Long = CLICK_TIME_THRESHOLD,
        callback: () -> Unit,
    ): MotionEvent {
        when (actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (eventTime - downTime <= clickTimeThreshold) {
                    callback.invoke()
                }
            }
        }
        return this
    }

    inline fun MotionEvent.onRelease(
        clickTimeThreshold: Long = CLICK_TIME_THRESHOLD,
        callback: () -> Unit,
    ): MotionEvent {
        when (actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (eventTime - downTime > clickTimeThreshold) {
                    callback.invoke()
                }
            }
        }
        return this
    }
}

object ClickAnimationConstants {
    const val CLICK_ANIMATION_INITIAL_VELOCITY = -2F
    const val CLICK_ANIMATION_STIFFNESS = 500F
    const val CLICK_ANIMATION_DAMPING_RATIO = .3F
    const val CLICK_TIME_THRESHOLD = 100L
}