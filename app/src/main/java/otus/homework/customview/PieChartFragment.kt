package otus.homework.customview

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import otus.homework.customview.databinding.FragmentPieChartBinding

/**
 * A simple [Fragment] subclass.
 * Use the [PieChartFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PieChartFragment : Fragment() {
    private var _binding: FragmentPieChartBinding? = null
    private val binding get() = _binding!!
    private var purchaseDto: Array<PurchaseDto>? = null
    private val pieChartData = mutableMapOf<String, Int>()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        val fakeRepository = FakeRepository(context)
        purchaseDto = fakeRepository.getData()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create dataset for the pie-chart from dto
        purchaseDto?.forEach {
            if (pieChartData.containsKey(it.category)) {
                val currentAmount = pieChartData[it.category]!!
                pieChartData[it.category] = currentAmount + it.amount
            } else {
                pieChartData[it.category] = it.amount
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPieChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // The custom view state is defining by dataset.
        // We set data when activity first start only.
        // The custom view will save it's state on device config change.
        if (savedInstanceState == null) {
            binding.pieChartCustomView.updateData(
                data = pieChartData
            )
        }

        binding.pieChartCustomView.setOnCategoryClickListener {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
        }
    }
}