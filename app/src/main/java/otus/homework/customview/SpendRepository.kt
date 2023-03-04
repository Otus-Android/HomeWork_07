package otus.homework.customview

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import otus.homework.customview.models.DailySpendItem
import otus.homework.customview.models.SpendItem
import java.lang.reflect.Type
import kotlin.random.Random

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

    fun getSpendItemsLinear(): List<DailySpendItem> {
        val categories = spendItems.map { it.category }.distinct()

        val spendList = mutableListOf<DailySpendItem>()

        categories.forEach { category ->
            repeat(15) {
                val item = DailySpendItem(
                    category = category,
                    dayNum = it + 1,
                    amount = Random.nextInt(200)
                )

                spendList.add(item)
            }
        }

        return spendList.toList()
    }
}
