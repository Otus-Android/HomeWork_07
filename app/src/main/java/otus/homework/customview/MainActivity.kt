package otus.homework.customview

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.data.CategoryForList
import otus.homework.customview.data.GraphBarModel
import otus.homework.customview.data.PayloadModel
import otus.homework.customview.data.PieChartModel
import otus.homework.customview.utils.toDateOnlyDayString

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pieChartView = findViewById<PieChartView>(R.id.pc)
        val graphBarView = findViewById<GraphBarView>(R.id.gb)
        val button = findViewById<Button>(R.id.button)

        val raw = this.resources.openRawResource(R.raw.payload).bufferedReader().use {
            it.readText()
        }
        val type = object : TypeToken<List<PayloadModel>>() {}.type
        val gson = Gson()
        val list = gson.fromJson<List<PayloadModel>>(raw, type)
        val pieChartList: MutableList<PieChartModel> = mutableListOf()
        val graphBarList: MutableList<GraphBarModel> = mutableListOf()
        val sumAll = list.sumOf { it.amount }
        val sumMax = list.maxOf { it.amount }

        list.map { payload ->
            val pieChartModel = pieChartList.find { payload.category == it.category }
            if (pieChartModel == null) {
                pieChartList.add(
                    PieChartModel(
                        category = payload.category,
                        color = 1,
                        percent = 0f,
                        sum = payload.amount
                    )
                )
            } else {
                pieChartList.forEachIndexed { index, it ->
                    if (it.category == payload.category) {
                        pieChartList[index] = it.copy(sum = it.sum + payload.amount)
                    }
                }
            }

            val graphBarModel = graphBarList.find { payload.category == it.category }
            if (graphBarModel == null) {
                graphBarList.add(
                    GraphBarModel(
                        category = payload.category,
                        color = 1,
                        listCategory = mutableListOf(
                            CategoryForList(
                                day = payload.time.toDateOnlyDayString().toInt(),
                                sum = payload.amount
                            )
                        )
                    )
                )
            } else {
                graphBarList.map { model ->
                    if (model.category == payload.category) {
                        model.listCategory.forEachIndexed { index, it ->
                            if (it.day == payload.time.toDateOnlyDayString().toInt()){
                                model.listCategory[index] = it.copy(sum = model.listCategory[index].sum + payload.amount)
                            } else {
                                model.listCategory.add(
                                    CategoryForList(
                                        day = payload.time.toDateOnlyDayString().toInt(),
                                        sum = payload.amount
                                    )
                                )
                            }
                        }

                    }
                }
            }
        }

        val shuffled = colorList.shuffled()
        pieChartList.forEachIndexed { index, model ->
            pieChartList[index] = model.copy(
                color = shuffled[index],
                percent = model.sum.toFloat() / sumAll,
            )
        }
        graphBarList.forEachIndexed { index, graphBarModel ->
            graphBarList[index] = graphBarModel.copy(
                color = shuffled[index]
            )
        }

        pieChartView.setList(pieChartList.sortedBy { it.percent }.reversed())

        graphBarView.setMaxPrice(sumMax)
        graphBarView.setList(graphBarList)
//        graphBarView.setList(fakeGraph)

        button.setOnClickListener {
            pieChartView.visibility = if (pieChartView.isVisible) View.GONE else View.VISIBLE
            graphBarView.visibility = if (graphBarView.isVisible) View.GONE else View.VISIBLE
        }
    }
}

private val fakeGraph = mutableListOf(
    GraphBarModel(
        category = "rrr",
        color = -65536,
        listCategory = mutableListOf(
            CategoryForList(
                day = 1,
                sum = 6000
            ),
            CategoryForList(
                day = 2,
                sum = 3000
            ),
            CategoryForList(
                day = 3,
                sum = 600
            ),
            CategoryForList(
                day = 4,
                sum = 1500
            ),
            CategoryForList(
                day = 5,
                sum = 6000
            ),
            CategoryForList(
                day = 6,
                sum = 2200
            ),
            CategoryForList(
                day = 7,
                sum = 2500
            ),
            CategoryForList(
                day = 8,
                sum = 3400
            ),
            CategoryForList(
                day = 9,
                sum = 800
            ),
            CategoryForList(
                day = 10,
                sum = 1200
            ),
            CategoryForList(
                day = 11,
                sum = 7000
            ),
            CategoryForList(
                day = 12,
                sum = 6500
            ),
            CategoryForList(
                day = 13,
                sum = 4200
            ),
            CategoryForList(
                day = 14,
                sum = 3100
            ),
            CategoryForList(
                day = 15,
                sum = 1500
            ),
            CategoryForList(
                day = 16,
                sum = 2700
            ),
            CategoryForList(
                day = 17,
                sum = 4900
            ),
            CategoryForList(
                day = 18,
                sum = 8000
            ),
            CategoryForList(
                day = 19,
                sum = 300
            ),
        )
    )
)

private val colorList = mutableListOf(
    Color.YELLOW,
    Color.RED,
    Color.GREEN,
    Color.BLACK,
    Color.BLUE,
    Color.CYAN,
    Color.GRAY,
    Color.MAGENTA,
    Color.DKGRAY,
    Color.LTGRAY
)