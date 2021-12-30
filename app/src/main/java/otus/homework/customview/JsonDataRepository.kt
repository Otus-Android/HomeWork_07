package otus.homework.customview

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import otus.homework.customview.di.ActivityScope
import javax.inject.Inject

@ActivityScope
class JsonDataRepository @Inject constructor(private val context: Context) {

    operator fun invoke(): List<Payload> {
        val stream = context.resources.openRawResource(R.raw.payload)
        return Gson().fromJson(stream.reader(), object : TypeToken<List<Payload>>() {}.type)
    }
}