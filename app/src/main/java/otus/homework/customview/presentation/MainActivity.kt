package otus.homework.customview.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import otus.homework.customview.data.UiDataRepo
import otus.homework.customview.databinding.ActivityMainBinding
import otus.homework.customview.getComponent
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var repo: UiDataRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        application.getComponent().inject(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.post {
            binding.pieChart.setSegments(repo.getUiData())
            binding.pieChart.setOnClickEvent { text ->

                Snackbar.make(
                    this,
                    binding.root,
                    text,
                    1500
                ).show()

            }
        }
    }
}