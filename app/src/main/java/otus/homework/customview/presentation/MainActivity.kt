package otus.homework.customview.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import otus.homework.customview.Event
import otus.homework.customview.di.DaggerActivityComponent
import ru.otus.daggerhomework.databinding.ActivityMainBinding
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var viewModelFactory: MainActivityViewModelFactory

    private lateinit var viewModel: MainActivityViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerActivityComponent.factory().create(this).inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainActivityViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.pieChartView.setOnClickListener(::onProductClick)

        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { result: UIStateResult ->
                    when (result) {
                        is UIStateResult.ClientProductList -> {
                            binding.pieChartView.setValues(result.productList)
                            binding.pieChartView.startAnimation()
                        }
                        UIStateResult.Idle -> {
                            // doNothing
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadProducts()
    }

    private fun onProductClick(event: Event) {
        when (event) {
            is Event.ProductClick -> {
                Toast.makeText(this, event.category, Toast.LENGTH_SHORT).show()
            }
        }
    }
}