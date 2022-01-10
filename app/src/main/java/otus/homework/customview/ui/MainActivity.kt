package otus.homework.customview.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import otus.homework.customview.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<BottomNavigationView>(R.id.bottomNavigation)
            .setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.fragment_pie_chart -> show(PieChartFragment())
                    R.id.fragment_line_chart -> { /* TODO */ }
                }
                true
            }

        if (savedInstanceState == null) show(PieChartFragment())
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.flContent, fragment)
            .commit()
    }

}