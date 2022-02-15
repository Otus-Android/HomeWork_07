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
    lateinit var pieChartViewModelFactory: PieChartViewModelFactory
    @Inject
    lateinit var lineChartViewModelFactory: LineChartViewModelFactory
    lateinit var mainActivityComponent: ActivityComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityComponent = DaggerActivityComponent.factory().create(this)
        mainActivityComponent.inject(this)
        setContentView(R.layout.activity_main)
        initPieChartView()
        initLineChartView()
    }

    private fun initPieChartView() {
        val view = findViewById<PieChartView>(R.id.pie_chart_view)
        view.pieChartViewModel = ViewModelProvider(this, pieChartViewModelFactory).get(PieChartViewModel::class.java)
        view.onInit()
    }

    private fun initLineChartView() {
        val view = findViewById<LineChartView>(R.id.line_chart_view)
        view.lineChartViewModel = ViewModelProvider(this, lineChartViewModelFactory).get(LineChartViewModel::class.java)
        view.onInit()
    }
}