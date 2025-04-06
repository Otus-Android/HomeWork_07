package otus.homework.customview.presentation.timeLine

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


/**
 * A simple [Fragment] subclass.
 * Use the [TimeLineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TimeLineFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProvider(requireActivity(), TimeLineViewModel.factory)[TimeLineViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_line, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val timeLineView = view.findViewById<TimeLineView>(R.id.timeLine)
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.spendsInTimeLine.collect {
                    timeLineView.setData(it)
                }
            }
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = TimeLineFragment()
    }
}