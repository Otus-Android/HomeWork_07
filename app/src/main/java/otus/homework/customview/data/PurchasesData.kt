package otus.homework.customview.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.R
import otus.homework.customview.model.PurchaseModel
import otus.homework.customview.piechart.PurchasesForPieChartModel

class PurchasesData {

    private var purchases: List<PurchaseModel>? = null
    private var purchasesForPieChart: HashMap<PurchasesForPieChartModel, Float>? = null
    private var purchasesForLineGraph: HashMap<String, ArrayList<PurchaseModel>>? = null

    fun getPurchases(
        context: Context
    ): List<PurchaseModel> {
        try {
            if (purchases == null) {
                val jsonFile = context.resources.openRawResource(R.raw.payload).bufferedReader()
                purchases =
                    Gson().fromJson(jsonFile, object : TypeToken<List<PurchaseModel>>() {}.type)
                purchases = purchases?.sortedBy { it.amount.toInt() }
                jsonFile.close()
                purchases
            }
        } catch (e: Exception) {
            throw Exception("Failed to read from payload")
        }

        return purchases ?: throw Exception("not found purchases")
    }

    fun getPurchasesForPieChart(context: Context): Map<PurchasesForPieChartModel, Float> {
        if (purchasesForPieChart == null) {

            val purchasesGroupedByName = getPurchases(context)
                .groupBy { it.name }
                .mapValues {
                    it.value.map {
                        PurchasesForPieChartModel(
                            it.id,
                            it.name,
                            it.amount,
                            it.category
                        )
                    }
                        .reduce { acc, e ->
                            PurchasesForPieChartModel(
                                e.id,
                                e.name,
                                (acc.amount.toInt() + e.amount.toInt()).toString(),
                                e.category
                            )
                        }
                }.values
            val sum = purchasesGroupedByName.sumOf { it.amount.toInt() }

            purchasesForPieChart = LinkedHashMap(purchasesGroupedByName.size)
            for (purchase in purchasesGroupedByName) {
                (purchasesForPieChart as LinkedHashMap<PurchasesForPieChartModel, Float>)[purchase] =
                    purchase.amount.toFloat() * 360 / sum
            }
        }
        return purchasesForPieChart ?: throw Exception("not found purchases")
    }

    fun getPurchasesForLineGraph(
        context: Context,
        category: String
    ): Pair<Int, ArrayList<PurchaseModel>> {
        val purchasesGroupedByName = getPurchases(context)
        if (purchasesForLineGraph == null) {
            purchasesForLineGraph = HashMap()
            for (purchase in purchasesGroupedByName) {
                if (purchasesForLineGraph!![purchase.category] == null) {
                    purchasesForLineGraph!![purchase.category] = ArrayList()
                }
                purchasesForLineGraph!![purchase.category]!!.add(purchase)
            }
        }
        val maxAmount = HashMap<String, Int>()
        for (purchase in purchasesGroupedByName) {
            if (maxAmount[purchase.category] == null) {
                maxAmount[purchase.category] = 0
            }
            if (purchase.amount.toInt() > maxAmount[purchase.category]!!)
                maxAmount[purchase.category] = purchase.amount.toInt()
        }
        return maxAmount[category]!! to purchasesForLineGraph!![category]!!
    }

}

