package otus.homework.customview.data.graph

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.graphics.PathMeasure
import androidx.annotation.IntRange


class ChartAnimator : Animator(), AnimatableChart {
  private val animator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)


  override fun getStartDelay(): Long {
    return animator.startDelay
  }

  override fun setStartDelay(@IntRange(from = 0) startDelay: Long) {
    animator.startDelay = startDelay
  }

  override fun setDuration(@IntRange(from = 0) duration: Long): Animator {
    return animator.setDuration(duration)
  }

  override fun getDuration(): Long {
    return animator.duration
  }

  override fun setInterpolator(value: TimeInterpolator?) {
    animator.interpolator = value
  }

  override fun isRunning(): Boolean {
    return animator.isRunning
  }

  override fun getAnimation(animatedChart: PathAnimatable): Animator? {
    val linePath = animatedChart.getPath()
    val pathMeasure = PathMeasure(linePath, false)
    val endLength = pathMeasure.length

    if (endLength <= 0) return null

    animator.addUpdateListener { animation ->
      val animatedValue = animation.animatedValue as Float
      val animatedPathLength = animatedValue * endLength
      linePath.reset()
      pathMeasure.getSegment(0f, animatedPathLength, linePath, true)
      animatedChart.setAnimationPath(linePath)
    }

    return animator
  }
}