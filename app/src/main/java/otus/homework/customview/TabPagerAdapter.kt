package otus.homework.customview

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabPagerAdapter(
    fa: FragmentActivity,
    private var tabCount: Int
) : FragmentStateAdapter(fa) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> Tab1Fragment()
            1 -> Tab2Fragment()
            else -> Tab1Fragment()
        }
    }

    override fun getItemCount(): Int {
        return tabCount
    }
}