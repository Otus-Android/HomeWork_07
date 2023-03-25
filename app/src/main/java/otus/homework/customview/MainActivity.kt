package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    private val expenses by lazy { ExpensesRepository(this).getExpenses() }
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}