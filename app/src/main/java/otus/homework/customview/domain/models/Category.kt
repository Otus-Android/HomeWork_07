package otus.homework.customview.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Категория расходов
 *
 * @param name наименование категории
 * @param amount кол-во расходов
 * @param expenses расходы, относящиеся к категории
 */
@Parcelize
data class Category(
    val name: String,
    val amount: Long,
    val expenses: List<Expense>
) : Parcelable
