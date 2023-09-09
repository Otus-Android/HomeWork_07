package otus.homework.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = object : TypeToken<List<DetailsChart.PayItem>>() {}.type
        binding.chartDetails.setup(
            Gson().fromJson(intent.getStringExtra(KEY_LIST), type)?: listOf()
        )

    }


    companion object {
        const val KEY_LIST = "KEY_LIST"
    }

}
