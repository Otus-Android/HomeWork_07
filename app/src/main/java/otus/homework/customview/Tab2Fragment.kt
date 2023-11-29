package otus.homework.customview

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import otus.homework.customview.databinding.FragmentTab2Binding

class Tab2Fragment : Fragment(R.layout.fragment_tab2) {
    val chartModel: LineChartModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentTab2Binding.bind(view)
        binding.chartView.lineChartModel = chartModel

    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}