package otus.homework.customview.data.datasources

import android.content.res.Resources.NotFoundException
import com.fasterxml.jackson.core.exc.StreamReadException
import com.fasterxml.jackson.databind.DatabindException
import otus.homework.customview.data.ExpenseEntity
import java.io.IOException

interface ExpensesDataSource {

    @Throws(
        NotFoundException::class,
        IOException::class,
        StreamReadException::class,
        DatabindException::class
    )
    fun getExpenses(): List<ExpenseEntity>
}