package otus.homework.customview

import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import otus.homework.customview.linegraph.LineGraphView
import otus.homework.customview.piechart.PieChartView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lineGraphView = findViewById<LineGraphView>(R.id.line_graph_view)
        val customView = findViewById<PieChartView>(R.id.pie_chart_view).apply {
            onOrderClick = { category ->
                lineGraphView.setCategory(category)
                lineGraphView.visibility = View.VISIBLE
                ObjectAnimator.ofFloat(lineGraphView, "translationY", 1000F, 0F).apply {
                    duration = 2000
                    interpolator = BounceInterpolator()
                    start()
                }
            }
        }

        val kf1 = Keyframe.ofFloat(.5f, 360f)
        val kf2 = Keyframe.ofFloat(1f, 0f)
        val rotateHolder = PropertyValuesHolder.ofKeyframe("rotation", kf1, kf2)
        val scaleMoveX = PropertyValuesHolder.ofFloat("translationX", 1000F, 0F)
        ObjectAnimator.ofPropertyValuesHolder(customView, scaleMoveX, rotateHolder).apply {
            duration = 4000
            start()
        }
    }
}
