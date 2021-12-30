package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import androidx.lifecycle.ViewModelProvider
import otus.homework.customview.di.ActivityComponent
import otus.homework.customview.di.DaggerActivityComponent
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: PieChartViewModelFactory
    lateinit var mainActivityComponent: ActivityComponent

    private val lastTouchDownXY = FloatArray(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityComponent = DaggerActivityComponent.factory().create(this)
        mainActivityComponent.inject(this)
        setContentView(R.layout.activity_main)
        val view = findViewById<PieChartView>(R.id.pie_chart_view)
        view.pieChartViewModel = ViewModelProvider(this, viewModelFactory).get(PieChartViewModel::class.java)
        view.setOnTouchListener { view, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                lastTouchDownXY[0] = event.x
                lastTouchDownXY[1] = event.y
            }
            return@setOnTouchListener false
        }
        view.setOnClickListener {
            if(it is PieChartView) {
                onPieceOfPieClick(it.getCategory(lastTouchDownXY[0], lastTouchDownXY[1]))
            }
        }
        view.onInit()
    }

    private fun onPieceOfPieClick(category: String) {
        Log.d("iszx", category)
    }
}