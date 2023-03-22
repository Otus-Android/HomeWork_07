package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewPager.adapter = CustomViewsPageAdapter()
        binding.viewPager.isUserInputEnabled = false

        val names = arrayOf("Pie Chart", "Line Chart")
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = names[position]
        }.attach()

    }
    private inner class CustomViewsPageAdapter
        : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return 2
        }
        override fun createFragment(position: Int): Fragment {

            return when (position) {
                0 -> PieChartFragment()
                else -> LineChartFragment()
            }
        }
    }
}