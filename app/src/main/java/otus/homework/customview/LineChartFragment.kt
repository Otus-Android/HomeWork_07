package otus.homework.customview

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import otus.homework.customview.databinding.FragmentLineChartBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LineChartFragment : Fragment() {
    private var _binding: FragmentLineChartBinding? = null
    private val binding get() = _binding!!
    private lateinit var purchaseDto: Array<PurchaseDto>
    private val lineChartData = mutableMapOf<String, Int>()
    private val lineChartCategoryName = "Продукты"
    override fun onAttach(context: Context) {
        super.onAttach(context)
        val fakeRepository = FakeRepository(context)
        purchaseDto = fakeRepository.getData()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create dataset for the line-chart from the dto
        // by filtering initial dataset by needed category,
        // e.g. "Продукты"
        purchaseDto.sortedBy { it.time }
            .filter { it.category == lineChartCategoryName }
            .forEach {
                val stringDate = Date(it.time).toStringView()
                if (lineChartData.containsKey(stringDate)) {
                    val currentAmount = lineChartData[stringDate]!!
                    lineChartData[stringDate] = currentAmount + it.amount
                } else {
                    lineChartData[stringDate] = it.amount
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLineChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // The custom view state is defining by dataset.
        // We set data when activity first start only.
        // The custom view will save it's state on device config change.
        if (savedInstanceState == null) {
            binding.lineChartCustomView.setData(
                categoryName = lineChartCategoryName,
                spendingByTimeData = lineChartData
            )
        }
    }

    private fun Date.toStringView(): String {
        val stringPattern = "d MMM"
        val simpleDateFormat = SimpleDateFormat(stringPattern, Locale.getDefault())
        return simpleDateFormat.format(this)
    }
}