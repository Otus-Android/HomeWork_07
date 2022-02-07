package otus.homework.customview.data.provider

import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import otus.homework.customview.data.model.Item
import otus.homework.customview.util.*

class DataSource {

  private val items: Items by lazy { getItemsList() }

  private fun getItemsList(): Items {
    var list = emptyList<Item>()
    val json = this.javaClass.getResource("/res/raw/payload.json")?.readText(Charsets.UTF_8)

    json?.let { list = Json.decodeFromString(it) } ?: run {
      Log.e("DataSource", "Error parsing json from resources.")
    }

    return list
  }

  val mergedItemsByCategory: Map<Category, Int>
    get() {
      val data = mutableMapOf<Category, Int>()
      items
        .map { it.category }
        .map { name ->
          val filteredByCategory = items.filter { it.category == name }
          val categoryMerged = filteredByCategory.reduce { acc, item ->
            acc.amount += item.amount
            acc
          }
          data[name] = categoryMerged.amount
        }
      return data
    }


  fun providePieChartData(): Map<Item, ItemSegment> {
    val data = mutableMapOf<Item, ItemSegment>()
    val totalAmount = items
      .map { it.amount }
      .reduce { counter, number -> counter + number }

    if (totalAmount == 0) return emptyMap()

    val segment = 360f / totalAmount

    items.forEach {
      data[it] = it.amount * segment
    }

    return data
  }

  fun provideLineGraphData(): Map<Category, Pair<Items, Count>> {
    val data = mutableMapOf<Category, Pair<Items, Count>>()
    items
      .map { it.category }
      .forEach { cat ->
        val filteredByCategory = items.filter { it.category == cat }
        val itemWithMaxAmount = filteredByCategory.maxByOrNull { it.amount }
        val pair = Pair(filteredByCategory, itemWithMaxAmount?.amount ?: 0)
        data[cat] = pair
      }

    return data
  }

}

