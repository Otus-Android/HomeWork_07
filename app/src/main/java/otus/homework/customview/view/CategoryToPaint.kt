package otus.homework.customview.view

import android.graphics.Paint

object CategoryToPaint {

    private val mapCategoryToPaint = mutableMapOf<String, Paint>()

    fun setCategoryToPaint(category: String, paint: Paint){
        mapCategoryToPaint[category] = paint
    }

    fun getPaint(category: String) = mapCategoryToPaint[category]
}