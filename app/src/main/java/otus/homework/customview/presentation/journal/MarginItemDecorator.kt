package otus.homework.customview.presentation.journal

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * `ItemDecoration`, обеспечивающий отсупы в виде `margins`
 *
 * @param margin значение отступов в `px`
 */
class MarginItemDecorator(
    private val margin: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        with(outRect) {
            left = margin
            top = margin
            right = margin
            bottom = margin
        }
    }
}