package otus.homework.customview

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChartView
    private lateinit var graph: GraphView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pieChart = findViewById(R.id.pie_chart_view)
        graph = findViewById(R.id.graph_view)

        setupPieChart()
    }

    private fun setupPieChart() {
        val payload: String =
            resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }

        val payments = Gson().fromJson(payload, Array<Expense>::class.java).toList()
        val total = payments.sumBy { it.amount }

        val byCategory = payments.groupBy { it.category }

        val pieChartData = byCategory.mapTo(mutableListOf()) { category ->
            Pair(category.key, category.value.sumOf { it.amount } * 100f/ total )
        }

        pieChart.setData(total, pieChartData)
        runAnimation()
        pieChart.onClickListener = { category ->
            byCategory[category]?.let { list -> graph.setData(list.sortedBy { it.time }.associate { it.date to it.amount }) }
        }
    }

    private fun runAnimation() {
        val rotationAnimator = ObjectAnimator.ofFloat(pieChart, View.ROTATION, 0F, 360F)
        val scaleXAnimator = ObjectAnimator.ofFloat(pieChart, View.SCALE_X, 0F, 1F)
            .apply {
                interpolator = BounceInterpolator()
            }
        val scaleYAnimator = ObjectAnimator.ofFloat(pieChart, View.SCALE_Y, 0F, 1F)
            .apply {
                interpolator = BounceInterpolator()
            }
        val animatorSet = AnimatorSet()
        animatorSet.apply {
            startDelay = 300
            duration = 3000
        }
        animatorSet.playTogether(rotationAnimator, scaleXAnimator, scaleYAnimator)
        pieChart.visibility = View.VISIBLE
        animatorSet.start()
    }
}