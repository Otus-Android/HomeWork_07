package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.databinding.ActivityMainBinding
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configureTabLayout()
        val adapter = TabPagerAdapter(this, binding.tabLayout.tabCount)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager)
        { tab, position ->
            tab.text = MYTITLE.get(position)
        }.attach()
    }

    private fun configureTabLayout() {
        repeat(2) {
            binding.tabLayout.addTab(binding.tabLayout.newTab())
        }
    }

    companion object {
        private val MYTITLE = listOf<String>("PIE CHART", "LINE CHART")
        val myData = loadData()

        fun loadData(): List<PayLoad> {
            val gson = Gson()
            val type = object : TypeToken<List<PayLoad>>() {}.type
            MyApp.myResource.reset()
            val myJson = MyApp.myResource.bufferedReader(Charset.defaultCharset())
            return gson.fromJson(myJson, type)
        }
    }
}