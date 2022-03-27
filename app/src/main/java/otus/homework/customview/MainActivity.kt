package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<PieChart>(R.id.pieChart).setOnClickListener {
            findViewById<PieChart>(R.id.pieChart).setValues(listOf(5, 3, 1, 4, 1,  3))
        }
    }


}