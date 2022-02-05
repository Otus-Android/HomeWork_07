package otus.homework.customview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import otus.homework.customview.databinding.FragmentBottomsheetBinding
import otus.homework.customview.databinding.ItemOptionBinding
import otus.homework.customview.databinding.ItemSomeOtherBinding

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
        val buildor = DelegateAdaptorBuildor()

        val cells: List<Any> = listOf(
            Header1("Interpolators"),
            InterpolatorDelegateCell(InterpolatorEnum.LINEAR, InterpolatorEnum.LINEAR.name),
            InterpolatorDelegateCell(InterpolatorEnum.LINEAR_OUT_SLOW_IN,
                InterpolatorEnum.LINEAR_OUT_SLOW_IN.name),
            InterpolatorDelegateCell(InterpolatorEnum.ACCELERATE, InterpolatorEnum.ACCELERATE.name),
            InterpolatorDelegateCell(InterpolatorEnum.ACCELERATE_DEC,
                InterpolatorEnum.ACCELERATE_DEC.name),
            Header2("Grouping by categories"),

        )

        buildor.setNewCells(cells) {
            registerTypeAndHoldar(InterpolatorDelegateCell::class) { cellProvider ->
                HoldarCreator { viewGroup ->
                    val binding: ItemOptionBinding = ItemOptionBinding.inflate(layoutInflater,
                        viewGroup,
                        false)
                    fastHolderBuilder(binding.root) { position ->
                        val interpolatorCell = cellProvider.getItem(position)
                        binding.textTitle.text = interpolatorCell.name
                        binding.root.setOnClickListener {
                            (requireActivity() as? MainActivity)?.onSelectInterpolator(
                                interpolatorCell.type)
                            Toast.makeText(view.context,
                                "interpolator: ${interpolatorCell.name} selected",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            registerTypeAndHoldar(Header1::class) { cellProvider ->
                HoldarCreator { viewGroup ->
                    val binding: ItemSomeOtherBinding = ItemSomeOtherBinding.inflate(layoutInflater,
                        viewGroup,
                        false)
                    fastHolderBuilder(binding.root) { position ->
                        val someOtherCell = cellProvider.getItem(position)
                        binding.text.text = someOtherCell.text
                    }
                }
            }
            registerTypeAndHoldar(Header2::class) { cellProvider ->
                HoldarCreator { viewGroup ->
                    val binding: ItemSomeOtherBinding = ItemSomeOtherBinding.inflate(layoutInflater,
                        viewGroup,
                        false)
                    fastHolderBuilder(binding.root) { position ->
                        val someOtherCell = cellProvider.getItem(position)
                        binding.text.text = someOtherCell.text
                    }
                }
            }
        }
        val adaptor = buildor.buildDelegateAdaptor()
        binding.interpolatorList.adapter = adaptor
        binding.interpolatorList.layoutManager = LinearLayoutManager(view.context)
    }
}

data class Header1(val text: String)
data class Header2(val text: String)
