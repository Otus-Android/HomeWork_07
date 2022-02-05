package otus.homework.customview

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import org.json.JSONArray
import org.json.JSONException


class MainActivity : AppCompatActivity() {
    lateinit var pieChartView: PieChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pieChartView = findViewById(R.id.pie_chart)

        val jsonString = resources.openRawResource(R.raw.payload).bufferedReader().use {
            it.readText()
        }
        val jsonArray = try {
            JSONArray(jsonString)
        } catch (e: JSONException) {
            Toast.makeText(this, "Couldn't read resources json", Toast.LENGTH_SHORT).show()
            null
        }
        val chartData = jsonArray?.let {
            val list = mutableListOf<ChartData>()
            try {
                for (i in 0 until it.length()) {
                    val obj = it.getJSONObject(i)
                    list.add(
                        ChartData(
                            amount = obj.getInt("amount"),
                            name = obj.getString("name"),
                            id = obj.getInt("id"),
                            category = obj.getString("category")
                        )
                    )
                }
            } catch (e: JSONException) {
                Toast.makeText(this, "Error when parsing", Toast.LENGTH_SHORT).show()
            }
            list
        }
        chartData?.let {
            pieChartView.setData(it)
        }

        val seekBar = findViewById<SeekBar>(R.id.seek)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                pieChartView.setOffset(progress * 3.6f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun onSelectInterpolator(type: InterpolatorEnum) {
        pieChartView.interpolator = when (type) {
            InterpolatorEnum.LINEAR -> LinearInterpolator()
            InterpolatorEnum.ACCELERATE_DEC -> AccelerateDecelerateInterpolator()
            InterpolatorEnum.ACCELERATE -> AccelerateInterpolator()
            InterpolatorEnum.LINEAR_OUT_SLOW_IN -> LinearOutSlowInInterpolator()
            InterpolatorEnum.FAST_OUT_LINEAR_IN -> FastOutLinearInInterpolator()
            InterpolatorEnum.FAST_OUT_SLOW_IN -> FastOutSlowInInterpolator()
        }
    }

    fun onGroupByCategories(boolean: Boolean) {
        pieChartView.setGroupByCategories(boolean)
    }

    public enum class Items { GROUP_BY_CATEGORIES }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.settings) {
            val bottomSheetFragmentDialog = BottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(Items.GROUP_BY_CATEGORIES.name, pieChartView.getGroupByCategories())
                }
            }
            bottomSheetFragmentDialog.show(supportFragmentManager, "OPTIONS")
            return true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true;
    }
}

data class InterpolatorDelegateCell(
    val type: InterpolatorEnum,
    val name: String,
)
data class SomeOtherCell(val index: Int)
enum class InterpolatorEnum { LINEAR, ACCELERATE_DEC, ACCELERATE, LINEAR_OUT_SLOW_IN, FAST_OUT_LINEAR_IN, FAST_OUT_SLOW_IN}
