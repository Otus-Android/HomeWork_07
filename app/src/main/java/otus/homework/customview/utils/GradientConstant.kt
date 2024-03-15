package otus.homework.customview.utils

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import otus.homework.customview.R

class GradientConstant {

    companion object {

        fun goldGradient(activity: Context): GradientDrawable {
            val colors: IntArray = intArrayOf(
                ContextCompat.getColor(activity, R.color.gold_card_gradient_1),
                ContextCompat.getColor(activity, R.color.gold_card_gradient_2),
                ContextCompat.getColor(activity, R.color.gold_card_gradient_3),
                ContextCompat.getColor(activity, R.color.gold_card_gradient_4),
                ContextCompat.getColor(activity, R.color.gold_card_gradient_5)
            )
            val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.TL_BR, colors)
            val arr2 = FloatArray(8)
            arr2[0] = 0f
            arr2[1] = 30f
            arr2[2] = 0f
            arr2[3] = 30f
            arr2[4] = 30f
            arr2[5] = 30f
            arr2[6] = 30f
            arr2[7] = 30f
            //{mTopLeftRadius, mTopLeftRadius, mTopRightRadius, mTopRightRadius, mBottomRightRadius, mBottomRightRadius, mBottomLeftRadius, mBottomLeftRadius}
            gradientDrawable.cornerRadii = arr2
            gradientDrawable.setStroke(
                3,
                ContextCompat.getColor(activity, R.color.gold_card_gradient_5)
            )

            return gradientDrawable
        }

        fun platinumGradient(activity: Context): GradientDrawable {
            val colors: IntArray = intArrayOf(
                ContextCompat.getColor(activity, R.color.platinum_gradient1),
                ContextCompat.getColor(activity, R.color.platinum_gradient2),
                ContextCompat.getColor(activity, R.color.platinum_gradient3),
                ContextCompat.getColor(activity, R.color.platinum_gradient4),
                ContextCompat.getColor(activity, R.color.platinum_gradient5)
            )
            val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.BR_TL, colors)
            val arr2 = FloatArray(8)
            arr2[0] = 30f
            arr2[1] = 30f
            arr2[2] = 30f
            arr2[3] = 30f
            arr2[4] = 30f
            arr2[5] = 30f
            arr2[6] = 30f
            arr2[7] = 30f
            gradientDrawable.cornerRadii = arr2
            return gradientDrawable
        }

    }
}