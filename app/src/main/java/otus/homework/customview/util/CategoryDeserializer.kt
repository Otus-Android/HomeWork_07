package otus.homework.customview.util

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import otus.homework.customview.task_1.model.Category
import java.lang.reflect.Type

class CategoryDeserializer : JsonDeserializer<Category> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Category? {
        var category: Category? = null
        json?.asString?.let {
            category = Category.nameOf(it)
        }
        return category
    }
}