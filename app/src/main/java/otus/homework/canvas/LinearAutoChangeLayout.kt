package otus.homework.canvas

import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout

class LinearAutoChangeLayout @JvmOverloads constructor(
    context: Context, private val attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var isVerticalOrientation: Boolean? = null

    init {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.LinearAutoChangeLayout)
        isVerticalOrientation = "vertical" == typedArray.getString(R.styleable.LinearAutoChangeLayout_baseOrientation)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val isRealyVerticalOrientation: Boolean = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        orientation = if (isVerticalOrientation == isRealyVerticalOrientation) VERTICAL else HORIZONTAL
        Log.d("***[", "width=$widthSize height=$heightSize $isVerticalOrientation $orientation $isRealyVerticalOrientation")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}