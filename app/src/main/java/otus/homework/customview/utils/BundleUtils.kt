package otus.homework.customview.utils

import android.os.Bundle
import androidx.core.os.bundleOf


fun Map<Int, Int>.toBundle(): Bundle = bundleOf(*this.map { it.key.toString() to it.value }.toTypedArray())

fun Bundle.toMap(): Map<Int, Int> = this.keySet().associate { it.toInt() to this.getInt(it) }