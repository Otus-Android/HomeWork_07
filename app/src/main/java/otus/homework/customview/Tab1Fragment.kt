package otus.homework.customview

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import otus.homework.customview.databinding.FragmentTab1Binding
import otus.homework.customview.piechart.ChartModel

class Tab1Fragment : Fragment(R.layout.fragment_tab1) {
    val chartModel: ChartModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTab1Binding.bind(view)
        binding.chartView.chartModel = chartModel

        val touchDown = binding.chartView._clickSector
        touchDown.observe(viewLifecycleOwner, {
            chartModel.setChecked(it)
            chartModel.setScale(it)
            binding.chartView.invalidate()
        })
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}