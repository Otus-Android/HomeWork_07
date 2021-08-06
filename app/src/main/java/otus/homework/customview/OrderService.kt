package otus.homework.customview

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception

class OrderService {

    private var orders: List<Order>? = null
    private var pieOrders: HashMap<Order, Float>? = null
    private var graphOrders: HashMap<String, ArrayList<Order>>? = null

    // Можно было бы сделать универсальный сервис, который не знает, откуда принимает данные
    // Можно было бы сделать разделение по какому-нибудь паттерну
    // Но задача ДЗ не в этом. А в том, чтобы уметь рисовать - это см. в PieChartView
    private fun getOrders(): List<Order> {
        if (orders == null) {
            val json = this.javaClass.getResource("/res/raw/payload.json").readText(Charsets.UTF_8)
            orders = Gson().fromJson(json, object: TypeToken<List<Order>>(){}.type)
            orders = orders?.sortedBy { it.amount.toInt() }
        }
        return orders ?: throw Exception("no orders")
    }

    fun getOrdersForPie(): Map<Order, Float> {
        if (pieOrders == null) {
            // Расчёт суммы всех заказов
            var sum = 0
            for (order in getOrders()) {
                sum += order.amount.toInt()
            }

            // Расчёт градуса каждого заказа
            pieOrders = LinkedHashMap(getOrders().size)
            for (order in getOrders()) {
                pieOrders!![order] = order.amount.toFloat() * 360 / sum
            }
        }
        return pieOrders ?: throw Exception("no orders")
    }

    fun getOrdersForGraph(category : String): Pair<Int, ArrayList<Order>> {
        if (graphOrders == null) {
            graphOrders = HashMap()
            for (order in getOrders()) {
                if (graphOrders!![order.category] == null) {
                    graphOrders!![order.category] = ArrayList()
                }
                graphOrders!![order.category]!!.add(order)
            }
        }
        val max = HashMap<String, Int>()
        for (order in getOrders()) {
            if (max[order.category] == null) {
                max[order.category] = 0
            }
            if (order.amount.toInt() > max[order.category]!!) max[order.category] = order.amount.toInt()
        }
        Log.d("", "" + max[category] + " " + graphOrders!![category])
        return Pair(max[category]!!, graphOrders!![category]!!)
    }
}