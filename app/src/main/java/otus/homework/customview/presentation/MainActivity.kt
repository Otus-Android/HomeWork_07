package otus.homework.customview.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import otus.homework.customview.R
import otus.homework.customview.databinding.ActivityMainBinding
import otus.homework.customview.presentation.pie.PieChartFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.setOnNavigationItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.pie_chart_menu_item -> PieChartFragment.newInstance().replace()
                R.id.line_chart_menu_item -> {}
            }
            true
        }
    }

    private fun Fragment.replace() = supportFragmentManager.beginTransaction()
        .replace(R.id.charts_container, this)
        .commit()
}