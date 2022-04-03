package otus.homework.customview

import androidx.annotation.RawRes

interface ResourceWrapper {
    fun openRawResource(@RawRes resId: Int): String
}