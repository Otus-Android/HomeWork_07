package otus.homework.customview.models

import com.google.gson.annotations.SerializedName

/**
 * Модель данных для расхода, загружаемого из внешнего источника.
 *
 * @property id Уникальный идентификатор расхода.
 * @property name Название товара или услуги.
 * @property amount Сумма расхода.
 * @property category Категория, к которой относится расход.
 * @property time Время совершения покупки (Unix timestamp).
 */
data class Expense(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("category") val category: String,
    @SerializedName("time") val time: Long
)
