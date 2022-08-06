package otus.homework.customview

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import androidx.core.view.MenuProvider
import androidx.fragment.app.setFragmentResultListener
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import otus.homework.customview.databinding.FragmentPieChartBinding

class PieChartFragment : Fragment() {
    private lateinit var binding : FragmentPieChartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPieChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu, menu)
        }

        override fun onMenuClosed(menu: Menu) {
            menu.removeItem(R.id.settings)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return if (menuItem.itemId == R.id.settings) {
                val bottomSheetFragmentDialog = BottomSheetFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(Items.GROUP_BY_CATEGORIES.name, this@PieChartFragment.binding.pieChart.getGroupByCategories())
                    }
                }
                bottomSheetFragmentDialog.show(childFragmentManager, "OPTIONS")
                bottomSheetFragmentDialog.setFragmentResultListener(BottomSheetFragment.RESULT) { requestKey: String, bundle: Bundle ->
                    if (bundle.containsKey("group_by_categories")) {
                        val group = bundle.getBoolean("group_by_categories")
                        onGroupByCategories(group)
                    }
                    if (bundle.containsKey("interpolator")){
                        val interpolator = bundle.getSerializable("interpolator") as InterpolatorEnum
                        onSelectInterpolator(interpolator)
                    }
                }
                true
            } else {
                false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pieChartView = binding.pieChart

        val chartData = MainActivity.getChartData(requireContext())
        chartData.let {
            pieChartView.setData(it)
        }

        val seekBar = binding.seek
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                pieChartView.setOffset(progress * 3.6f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        pieChartView.sectorClickListener = object  : PieChartView.SectorClickListener {
            override fun onSectorClick(sectorName: String) {
            }
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.addMenuProvider(menuProvider)
    }

    override fun onStop() {
        super.onStop()
        activity?.removeMenuProvider(menuProvider)
    }

    fun onGroupByCategories(boolean: Boolean) {
        binding.pieChart.setGroupByCategories(boolean)
    }

    public enum class Items { GROUP_BY_CATEGORIES }

    fun onSelectInterpolator(type: InterpolatorEnum) {
        binding.pieChart.interpolator = when (type) {
            InterpolatorEnum.LINEAR -> LinearInterpolator()
            InterpolatorEnum.ACCELERATE_DEC -> AccelerateDecelerateInterpolator()
            InterpolatorEnum.ACCELERATE -> AccelerateInterpolator()
            InterpolatorEnum.LINEAR_OUT_SLOW_IN -> LinearOutSlowInInterpolator()
            InterpolatorEnum.FAST_OUT_LINEAR_IN -> FastOutLinearInInterpolator()
            InterpolatorEnum.FAST_OUT_SLOW_IN -> FastOutSlowInInterpolator()
        }
    }
}

data class InterpolatorDelegateCell(
    val type: InterpolatorEnum,
    val name: String,
)
data class SomeOtherCell(val index: Int)
enum class InterpolatorEnum { LINEAR, ACCELERATE_DEC, ACCELERATE, LINEAR_OUT_SLOW_IN, FAST_OUT_LINEAR_IN, FAST_OUT_SLOW_IN}
