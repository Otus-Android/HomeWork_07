package otus.homework.customview

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import otus.homework.customview.model.Store

class DiContainer {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val type = Types.newParameterizedType(List::class.java, Store::class.java)

    val adapter: JsonAdapter<List<Store>> = moshi.adapter(type)
}