package otus.homework.customview.data.datasources

import android.content.Context
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import otus.homework.customview.R
import otus.homework.customview.data.ExpenseEntity

class LocalDataSource(
    private val context: Context,
    private val objectMapper: ObjectMapper
) : ExpensesDataSource {

    private val typeReference = object : TypeReference<List<ExpenseEntity>>() {}

    override fun getExpenses(max: Int?): List<ExpenseEntity> =
        getExpenses().also { expenses -> max?.let { expenses.take(max) } }

    private fun getExpenses(): List<ExpenseEntity> =
        objectMapper.readValue(context.resources.openRawResource(R.raw.payload), typeReference)
}