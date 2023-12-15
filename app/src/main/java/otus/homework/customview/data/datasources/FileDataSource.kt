package otus.homework.customview.data.datasources

import android.content.Context
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import otus.homework.customview.R
import otus.homework.customview.data.models.ExpenseEntity
import otus.homework.customview.data.models.ExpensesDataException

/**
 * Файловый источник данных
 *
 * @param context `application` context
 * @param objectMapper `JSON` преобразователь
 */
class FileDataSource(
    private val context: Context,
    private val objectMapper: ObjectMapper
) : ExpensesDataSource {

    private val typeReference = object : TypeReference<List<ExpenseEntity>>() {}

    override fun getExpenses(max: Int?): List<ExpenseEntity> =
        getExpenses().also { expenses -> max?.let { expenses.take(max) } }

    private fun getExpenses(): List<ExpenseEntity> = try {
        objectMapper.readValue(context.resources.openRawResource(R.raw.payload), typeReference)
    } catch (e: Exception) {
        throw ExpensesDataException(e)
    }
}