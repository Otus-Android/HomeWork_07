package otus.homework.customview.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.otus.homework.R

class MainFragment : Fragment() {
    private var param1: (() -> Unit)? = null
    private var param2: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button4).setOnClickListener { param1?.invoke() }
        view.findViewById<Button>(R.id.button5).setOnClickListener { param2?.invoke() }
    }

    companion object {
        fun create(firstCallback: () -> Unit, secondCallback: () -> Unit): MainFragment {
            return MainFragment().apply {
                param1 = firstCallback
                param2 = secondCallback
            }
        }
    }
}