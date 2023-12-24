package otus.homework.customview.presentation.customview

import android.content.Context

/**
 * Утилитный класс для часто используемых функций в кастомных вьюхах
 *
 * @author Евтушенко Максим 07.12.2023
 */
object CustomViewUtils {

    /**
     * Context Extension для конвертирования значения в пиксели.
     * @param dp - значение density-independent pixels
     */
    fun Context.dpToPx(dp: Int): Float = dp.toFloat() * this.resources.displayMetrics.density
}