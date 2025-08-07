package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import otus.homework.customview.view.PieChartView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null)
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    CirclePaymentCategoryFragment(),
                    "CirclePaymentCategoryFragment"
                )
                .addToBackStack(null)
                .commit()

        val button = findViewById<Button>(R.id.button_next)

        button.setOnClickListener {
            val isCirclePaymentCategoryFragmentCurrent = supportFragmentManager
                .findFragmentByTag("CirclePaymentCategoryFragment")
                ?.isVisible == true

            if (isCirclePaymentCategoryFragmentCurrent) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        GraphicPaymentCategoryFragment(),
                        "GraphicPaymentCategoryFragment"
                    )
                    .addToBackStack(null)
                    .commit()
            } else {
                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer,
                        CirclePaymentCategoryFragment(),
                        "CirclePaymentCategoryFragment"
                    )
                    .addToBackStack(null)
                    .commit()
            }

        }
    }
}