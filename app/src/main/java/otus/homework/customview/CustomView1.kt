package otus.homework.customview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.gson.Gson


class CustomView1: View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        val gson = Gson()
        val buffer: String = resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        val data: Array<PayLoad> = gson.fromJson(buffer, Array<PayLoad>::class.java)
        //val buffer: String = resources.openRawResource(R.raw.text).bufferedReader().use { it.readText() }
        //var data = gson.fromJson(buffer, PayLoad::class.java)
        println(data[0].name)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {



    }

}