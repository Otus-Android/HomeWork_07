package otus.homework.customview.data.graph

import androidx.annotation.IntDef

@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
@IntDef(FillType.NONE, FillType.UP, FillType.DOWN, FillType.TOWARD_ZERO)
annotation class FillType {
  companion object {
    const val NONE = 0
    const val UP = 1
    const val DOWN = 2
    const val TOWARD_ZERO = 3
  }
}
