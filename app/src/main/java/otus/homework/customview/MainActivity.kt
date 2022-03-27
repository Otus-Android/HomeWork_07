package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.google.gson.Gson
import java.io.FileReader
import android.content.res.Resources


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val categoryList = readJson()

        findViewById<Button>(R.id.showBtn).setOnClickListener {

            findViewById<PieChart>(R.id.pieChart).setValues(categoryList.toList())
        }
    }

    fun Resources.getRawTextFile( id: Int) =
        openRawResource(id).bufferedReader().use { it.readText() }

    fun readJson(): Array<Category> {
        val fileContent = resources.getRawTextFile(R.raw.payload)
        var gson = Gson();
        return gson?.fromJson(fileContent, Array<Category>::class.java)
    }


}