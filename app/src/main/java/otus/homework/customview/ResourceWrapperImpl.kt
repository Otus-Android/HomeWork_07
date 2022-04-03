package otus.homework.customview

import android.content.Context

class ResourceWrapperImpl(private val context: Context) : ResourceWrapper {
    override fun openRawResource(resId: Int): String {
        return context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
    }
}