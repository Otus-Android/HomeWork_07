package otus.homework.customview.presentation.firstPieChart

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.otus.homework.R
import kotlinx.coroutines.launch


class PieChartFragment : Fragment() {
    private val pieChartViewModel: PieChartViewModel by lazy {
        ViewModelProvider(requireActivity(), PieChartViewModel.Factory)[PieChartViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pie_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val customDiagram = view.findViewById<PieChartView>(R.id.pi_chart)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pieChartViewModel.spends.collect {
                    customDiagram.setData(it)
                }
            }
        }
    }
}