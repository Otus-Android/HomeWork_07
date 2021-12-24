package otus.homework.customview

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import otus.homework.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val diContainer = DiContainer()
    private val viewModel by viewModels<PieViewModel>{ PieViewModelFactory(diContainer.adapter) }

    private lateinit var storeJson: String
    private lateinit var storeGraphJson: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            storeJson = resources.openRawResource(R.raw.payload)
                .bufferedReader()
                .use { it.readText() }

            viewModel.storesSharedFlow.collect{
                binding.pieChart.setStores(it)
            }
        }

        lifecycleScope.launch {
            storeGraphJson = resources.openRawResource(R.raw.payload_graph)
                .bufferedReader()
                .use { it.readText() }

            viewModel.storesGraphSharedFlow.collect{
                binding.graph.setStores(it)
            }
        }

        viewModel.getStoresFromJson(storeJson, storeGraphJson)
    }
}