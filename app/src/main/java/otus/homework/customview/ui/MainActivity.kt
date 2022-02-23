package otus.homework.customview.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.R
import otus.homework.customview.data.models.Purchase
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    var purchases: Map<String, List<Purchase>> = mapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        purchases = loadPurchases()

        findViewById<BottomNavigationView>(R.id.bottomNavigation).apply {
            setOnNavigationItemReselectedListener {  }
            setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.fragment_pie_chart -> show(PieChartFragment())
                    R.id.fragment_line_chart -> show(LineChartFragment())
                }
                true
            }
        }

        if (savedInstanceState == null) show(PieChartFragment())
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.flContent, fragment)
            .commit()
    }

    private fun loadPurchases(): Map<String, List<Purchase>> {
        val dataType = object : TypeToken<List<Purchase>>() {}.type
        return InputStreamReader(resources.openRawResource(R.raw.payload)).use { reader ->
            Gson().fromJson<List<Purchase>>(reader, dataType)
                .groupBy { it.category }
        }
    }

}