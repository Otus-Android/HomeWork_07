package otus.homework.customview

import android.content.Context
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
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import org.json.JSONArray
import org.json.JSONException
import otus.homework.customview.databinding.ActivityMainBinding
import java.util.Collections.emptyList


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = binding.fragmentNavhost.getFragment<NavHostFragment>()
        val navController = navHostFragment.navController

        val bottomNav = binding.bottombar
        bottomNav.setupWithNavController(navController)
    }

    companion object {
        fun getChartData(context: Context) : List<ChartData> {
            val jsonString = context.resources.openRawResource(R.raw.payload).bufferedReader().use {
                it.readText()
            }
            val jsonArray = try {
                JSONArray(jsonString)
            } catch (e: JSONException) {
                Toast.makeText(context, "Couldn't read resources json", Toast.LENGTH_SHORT).show()
                null
            }
            return jsonArray?.let {
                val list = mutableListOf<ChartData>()
                try {
                    for (i in 0 until it.length()) {
                        val obj = it.getJSONObject(i)
                        list.add(
                            ChartData(
                                amount = obj.getInt("amount"),
                                name = obj.getString("name"),
                                id = obj.getInt("id"),
                                category = obj.getString("category"),
                                time = obj.getLong("time")
                            )
                        )
                    }
                } catch (e: JSONException) {
                    Toast.makeText(context, "Error when parsing", Toast.LENGTH_SHORT).show()
                }
                list
            } ?: emptyList()
        }
    }
}

