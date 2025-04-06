package otus.homework.customview.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import com.otus.homework.R
import otus.homework.customview.presentation.pieChart.PieChartFragment
import otus.homework.customview.presentation.timeLine.TimeLineFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fragmentContainer = findViewById<FragmentContainerView>(R.id.fragment_container_view_tag)

        val callback1 = {
            supportFragmentManager.beginTransaction()
                .replace(fragmentContainer.id, PieChartFragment())
                .commit()
            Unit
        }

        val callback2 = {
            supportFragmentManager.beginTransaction()
                .replace(fragmentContainer.id, TimeLineFragment())
                .commit()
            Unit
        }

        supportFragmentManager
            .beginTransaction()
            .replace(fragmentContainer.id, MainFragment.create(callback1, callback2))
            .commit()
    }
}