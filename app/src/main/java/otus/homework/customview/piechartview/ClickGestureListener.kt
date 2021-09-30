package otus.homework.customview.piechartview

import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/**
 * Слушатель, очень похожий на View.ClickListener
 *
 * @author Юрий Польщиков on 15.09.2021
 */
class ClickGestureListener<T>(
    private val view: View,
    private val clickListener: ((T?) -> Unit)?
) : GestureDetector.SimpleOnGestureListener() {

    private val outRect = Rect()
    private val location = IntArray(2)

    var data: T? = null

    override fun onDown(event: MotionEvent?): Boolean {
        return event != null
    }

    override fun onSingleTapUp(event: MotionEvent?): Boolean {
        if (event != null) {
            clickListener?.invoke(data)
            return true
        }
        return false
    }

    override fun onScroll(
        event1: MotionEvent?,
        event2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return event2 != null && view.isInside(event2)
    }

    override fun onFling(
        event1: MotionEvent?,
        event2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (event2 != null && view.isInside(event2)) {
            clickListener?.invoke(data)
            return true
        }
        return false
    }

    override fun onDoubleTap(event: MotionEvent?): Boolean {
        if (event != null) {
            clickListener?.invoke(data)
            return true
        }
        return false
    }

    private fun View.isInside(event: MotionEvent): Boolean {
        getDrawingRect(outRect)
        getLocationOnScreen(location)
        outRect.offset(location[0], location[1])
        return outRect.contains(event.rawX.toInt(), event.rawY.toInt())
    }
}
