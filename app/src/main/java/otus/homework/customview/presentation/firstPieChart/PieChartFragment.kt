package otus.homework.customview.presentation.firstPieChart

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
        customDiagram.setOnSectorClickListener { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        val linearLayout = view.findViewById<LinearLayout>(R.id.history)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pieChartViewModel.spends.collect { pieData ->
                    customDiagram.setData(pieData)
                    pieData.pieSlices.forEach {
                        val categoryAndTotal = "${it.key}: ${it.value.amount}"
                        linearLayout.addView(
                            TextView(context).apply {
                                text = getTextWithIcon(categoryAndTotal, it.value.paint.color)
                                textSize = 18f
                                typeface = Typeface.SANS_SERIF
                                setTextColor(Color.BLACK)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun getTextWithIcon(text: String, color: Int): SpannableString {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(color)
            setSize(50, 50)
        }

        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

        val spannable = SpannableString("  $text")
        val imageSpan = ImageSpan(drawable, DynamicDrawableSpan.ALIGN_BASELINE)
        spannable.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        return spannable
    }
}