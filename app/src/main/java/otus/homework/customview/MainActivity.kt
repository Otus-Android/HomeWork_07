package otus.homework.customview

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val result = resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        val type = TypeToken.getParameterized(
            ArrayList::class.java,
            Item::class.java).type
        val items = Gson().fromJson<ArrayList<Item>>(result, type)

        val graphView = findViewById<GraphView>(R.id.graphView)
        graphView.setCoordinate(items)
        graphView.setItems(items, null)

        val pieChartView = findViewById<PieChartView>(R.id.pieChartView)
        pieChartView.setItems(items)
        pieChartView.categoryClickedListener = object : PieChartView.Listener {
            override fun clickCategory(category: Category) {
                graphView.setItems(category.items, category.color)
                Log.d("MainActivity", "category ${category.toString()}")
            }
        }

        val motionLayout = findViewById<MotionLayout>(R.id.motion_layout)

//        motionLayout.transitionToEnd()
        animationAlpha(pieChartView)
        animationScale(pieChartView)
    }

    fun animationAlpha(view: View) {
        ObjectAnimator.ofFloat(view, View.ALPHA, 0.3F,  1F)
            .setDuration(1000)
            .apply {
                start()
            }
    }

    fun animationScale(view: View) {
        ValueAnimator.ofFloat(0.2F, 1F).apply {
            interpolator = LinearInterpolator()
            duration = 1000
            addUpdateListener {
                view.scaleX = it.animatedValue as Float
                view.scaleY = it.animatedValue as Float
            }
            start()
        }
    }
}