package otus.homework.customview.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.view.View
import androidx.constraintlayout.core.motion.utils.Utils


class Anime {

    companion object {
        fun setPageBackground(newBG: GradientDrawable, root: View?, type: Int) {
            if (root != null) {
                val currentBG = root.background
                //add your own logic here to determine the newBG
                if (currentBG == null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        root.background = newBG
                    } else {
                        root.background = newBG
                    }
                } else {
                    val transitionDrawable = TransitionDrawable(arrayOf(currentBG, newBG))
                    transitionDrawable.isCrossFadeEnabled = true
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        root.background = transitionDrawable
                    } else {
                        root.background = transitionDrawable
                    }
                    transitionDrawable.startTransition(400)
                }
            }
        }
    }
}