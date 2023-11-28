package otus.homework.customview

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Comparator


class MainActivity : AppCompatActivity() {
    private var _binding : ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding?:throw RuntimeException("binding = null")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val inputStream = this.resources.openRawResource(R.raw.payload)

       val jsonStr = try {
            val reader = BufferedReader(InputStreamReader(inputStream))
           val str = StringBuilder()
           var line = reader.readLine()
            while (line!=null){
                str.append(line)
                line = reader.readLine()
            }
           str.toString()

        }catch (e: Throwable){
            ""
        }finally {
                inputStream.close()
        }

        val typeToken = object : TypeToken<List<Expense>>() {}.type
        val expenses = Gson().fromJson<List<Expense>>(jsonStr, typeToken)

        val comparator = Comparator<Expense> { o1, o2 -> if (o1.amount>o2.amount) 1 else if (o2.amount>o1.amount) -1 else 0 }


        val list = listOf(12f,53f,1f,12f,33f,33f,33f)
        binding.myCustomView.setValues(expenses.sortedWith(comparator)) { exp ->
            Log.i(TAG, "It is from activity ___ ${exp.name} was chosen")
        }

    }




    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
