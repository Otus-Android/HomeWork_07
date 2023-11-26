package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var _binding : ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding?:throw RuntimeException("binding = null")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val list = listOf(12f,33f,33f,33f)
        binding.myCustomView.setValues(list.sorted())

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
