package otus.homework.customview.data

import android.content.res.Resources
import com.google.gson.Gson
import otus.homework.customview.domain.ClientProductsRepository
import otus.homework.customview.models.ProductDataModel
import ru.otus.daggerhomework.R

/**
 * Репозиторий для получения списка продуктов клиента
 *
 * @author Евтушенко Максим 16.12.2023
 */
class ClientProductsRepositoryImpl(private val resources: Resources) : ClientProductsRepository {
    override fun loadClientCategories(): List<ProductDataModel> {
        val gson = Gson()
        val buffer: String = resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        return gson.fromJson(buffer, Array<ProductDataModel>::class.java).toList()
    }
}