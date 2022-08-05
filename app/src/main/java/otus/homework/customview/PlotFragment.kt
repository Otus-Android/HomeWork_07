package otus.homework.customview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import otus.homework.customview.databinding.FragmentPlotBinding

class PlotFragment : Fragment() {
    private lateinit var binding : FragmentPlotBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPlotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val plotView = binding.plot

        val chartData = MainActivity.getChartData(requireContext())
        chartData.let {
            plotView.setData(it)
        }
    }
}
