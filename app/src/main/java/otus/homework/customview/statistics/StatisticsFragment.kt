package otus.homework.customview.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import otus.homework.customview.R
import otus.homework.customview.piechart.SegmentsDataEntity
import otus.homework.customview.databinding.FragmentStatisticsBinding

class StatisticsFragment : Fragment() {

    private var binding: FragmentStatisticsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val jsonData = context?.applicationContext?.resources
            ?.openRawResource(R.raw.payload)
            ?.bufferedReader()
            .use { it?.readText() }

        val uiData = Gson().fromJson(jsonData, SegmentsDataEntity::class.java)

        if (savedInstanceState == null) {
            binding?.statisticView?.setData(uiData)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}