package otus.homework.customview.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Модель расходов
 *
 * @param id идентификатор операции
 * @param name наименование
 * @param amount кол-во потраченных стредств
 * @param category наименование категории расходов
 * @param time время операции (unix метка, ms)
 */
@Parcelize
data class Expense(
    val id: Int,
    val name: String,
    val amount: Int,
    val category: String,
    val time: Long
) : Parcelable