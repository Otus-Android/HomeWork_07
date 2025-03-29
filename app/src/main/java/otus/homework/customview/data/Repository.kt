package otus.homework.customview.data

import android.app.Application
import android.content.Context
import com.otus.homework.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import otus.homework.customview.domain.Repository
import otus.homework.customview.domain.Spend
import kotlin.random.Random

class Repository(
    context: Context,
): Repository {
    private val json = context.resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
    private val spends = MutableStateFlow(Json.decodeFromString<List<Spend>>(json))

    init {
    }

    override fun getSpendsUseCase(): StateFlow<List<Spend>> {
        return spends.asStateFlow()
    }

    override fun generateNewSpends() {
        spends.value = spends.value.map {
            it.copy(amount = Random.nextInt(100, 10_000))
        }
    }
}