package otus.homework.customview

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import otus.homework.customview.models.SpendItem
import java.lang.reflect.Type

class SpendRepository(private val context: Context) {

    private val spendJson: String
        get() = context.resources
            .openRawResource(R.raw.payload)
            .bufferedReader()
            .use { it.readText() }


    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val spendItemListType: Type =
        Types.newParameterizedType(MutableList::class.java, SpendItem::class.java)
    private val adapter = moshi.adapter<List<SpendItem>>(spendItemListType)

    private val spendItems: List<SpendItem> = adapter.fromJson(spendJson)!!

    fun getSpendItems() = spendItems
}
