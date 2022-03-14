package otus.homework.customview.data.graph

import android.animation.Animator
import android.graphics.Path

interface PathAnimatable {
  fun setAnimationPath(animationPath: Path)
  fun getPath(): Path
}

interface AnimatableChart {
  fun getAnimation(animatedChart: PathAnimatable): Animator?
}