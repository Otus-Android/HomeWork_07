package otus.homework.customview.piechart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import otus.homework.customview.R
import otus.homework.customview.databinding.FragmentPieChartBinding

class PieChartFragment : Fragment() {

    private var binding: FragmentPieChartBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPieChartBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val applicationContext = context?.applicationContext

        val jsonData = applicationContext?.resources
            ?.openRawResource(R.raw.payload)
            ?.bufferedReader()
            .use { it?.readText() }

        val uiData = Gson().fromJson(jsonData, SegmentsDataEntity::class.java)


        binding?.customPieChartView?.setOnSegmentClickListener { segment ->
            Toast.makeText(applicationContext, segment.category, Toast.LENGTH_SHORT).show()
        }

        if (savedInstanceState == null) {
            binding?.customPieChartView?.setData(uiData)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}