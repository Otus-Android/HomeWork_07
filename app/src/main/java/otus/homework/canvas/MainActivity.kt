package otus.homework.canvas

import JsonData
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    val pieChartView: PieChartView by lazy { findViewById<PieChartView>(R.id.PieChartView) }
    val lineGraphView: LineGraphView by lazy { findViewById<LineGraphView>(R.id.LineGraphView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //onFull(null)
        onMyData(null)
    }


    fun onFull(view: View?) {
        val jsonData = JsonData(applicationContext)
        pieChartView.setData(jsonData.getPieChartSectorData(), -1F) {category, num ->  clickCallBack(category, num)}
        lineGraphView.setData(jsonData.getLineGraphData())

    }

    private fun clickCallBack(category: String, numSector: Int) {
        Log.d("***[", "clickCallBack category=$category numSector=$numSector")
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    200,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(200)
        }
    }

    fun onMyData(view: View?) {
        val sectors = arrayListOf<PieChartSectorData>(
            PieChartSectorData(10F, Color.RED, 50F, "10", "Sector №1"),
            PieChartSectorData(15F, Color.YELLOW, 40F, "15", "Sector №2"),
            PieChartSectorData(7F, Color.GREEN, 45F, "7", "Sector №3"),
            PieChartSectorData(18F, Color.BLUE, 52F, "18", "Sector №4"),
        )
        pieChartView.setData(sectors, 32F) {category, num ->  clickCallBack(category, num)}

        var lineGraphData = mutableMapOf<String, List<LineGraphData>>()

        lineGraphData += "param1" to arrayListOf(
            LineGraphData(5F,20F),
            LineGraphData(0F,12F),
            LineGraphData(4F,33F),
            LineGraphData(2F,55F),
            LineGraphData(3F,52F),
            LineGraphData(1F,40F),
        )
        var sinY = arrayListOf<LineGraphData>()
        for (t in 0 .. 500) {
            val y = sin(t.toFloat() / 50F) * 50F
            sinY.add(LineGraphData(t.toFloat()/100, y))
        }
        lineGraphData += "param2" to sinY
        lineGraphData += "param3" to arrayListOf(
            LineGraphData(5F,00F),
            LineGraphData(0F,20F),
            LineGraphData(4F,30F),
            LineGraphData(2F,60F),
            LineGraphData(3F,50F),
            LineGraphData(1F,40F),
        )
        lineGraphView.setData(lineGraphData)
    }
}

