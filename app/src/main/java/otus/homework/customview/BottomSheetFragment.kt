package otus.homework.customview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import otus.homework.customview.databinding.*

class BottomSheetFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentBottomsheetBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBottomsheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val buildor = TypedDelegateAdapterBuilder()
        val groupingIsOn =
            arguments?.getBoolean(PieChartFragment.Items.GROUP_BY_CATEGORIES.name, false) ?: false

        val cells: List<Any> = listOf(
            Header1("Interpolators"),
            InterpolatorDelegateCell(InterpolatorEnum.LINEAR, InterpolatorEnum.LINEAR.name),
            InterpolatorDelegateCell(InterpolatorEnum.LINEAR_OUT_SLOW_IN,
                InterpolatorEnum.LINEAR_OUT_SLOW_IN.name),
            InterpolatorDelegateCell(InterpolatorEnum.ACCELERATE, InterpolatorEnum.ACCELERATE.name),
            InterpolatorDelegateCell(InterpolatorEnum.ACCELERATE_DEC,
                InterpolatorEnum.ACCELERATE_DEC.name),
            Header1("Grouping by categories"),
            Switch("Group by categories", groupingIsOn)
        )

        buildor.setNewCells(cells) {
            registerTypeAndHoldarCreator(InterpolatorDelegateCell::class) { viewGroup, cellProvider ->
                val binding: ItemOptionBinding = ItemOptionBinding.inflate(layoutInflater,
                    viewGroup,
                    false)
                createHoldar(binding.root) { position ->
                    val interpolatorCell = cellProvider.getItem(position)
                    binding.textTitle.text = interpolatorCell.name
                    binding.root.setOnClickListener {
                        setFragmentResult(RESULT, Bundle().also { it.putSerializable("interpolator", interpolatorCell.type) } )
                        Toast.makeText(view.context,
                            "interpolator: ${interpolatorCell.name} selected",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
            registerTypeAndHoldarCreator(Header1::class) { viewGroup, cellProvider ->
                val binding: ItemHeader1Binding = ItemHeader1Binding.inflate(layoutInflater,
                    viewGroup,
                    false)
                createHoldar(binding.root) { position ->
                    val someOtherCell = cellProvider.getItem(position)
                    binding.text.text = someOtherCell.text
                }

            }
            registerTypeAndHoldarCreator(Header2::class) { viewGroup, cellProvider ->
                val binding: ItemHeader2Binding = ItemHeader2Binding.inflate(layoutInflater,
                    viewGroup,
                    false)
                createHoldar(binding.root) { position ->
                    val someOtherCell = cellProvider.getItem(position)
                    binding.text.text = someOtherCell.text
                }

            }
            registerTypeAndHoldarCreator(Switch::class) { viewGroup, cellProvider ->
                val binding: ItemFlagBinding = ItemFlagBinding.inflate(layoutInflater, viewGroup, false)
                createHoldar(binding.root) { position ->
                    val cell = cellProvider.getItem(position)
                    binding.text.text = cell.text
                    binding.switchButton.setOnCheckedChangeListener(null)
                    binding.switchButton.isChecked = cell.on
                    val callback =
                        CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
                            setFragmentResult(RESULT, Bundle().also { it.putBoolean("group_by_categories", isChecked) } )
                        }
                    binding.switchButton.setOnCheckedChangeListener(callback)
                    binding.root.setOnClickListener {
                        binding.switchButton.isChecked = !binding.switchButton.isChecked
                    }
                }
            }
        }
        val adaptor = buildor.buildDelegateAdaptor()
        binding.interpolatorList.adapter = adaptor
        binding.interpolatorList.layoutManager = LinearLayoutManager(view.context)
    }

    companion object {
        public const val RESULT = "settings_result"
    }
}

data class Header1(val text: String)
data class Header2(val text: String)
data class Switch(val text: String, val on: Boolean)
