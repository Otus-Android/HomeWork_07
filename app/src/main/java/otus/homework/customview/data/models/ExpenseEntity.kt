package otus.homework.customview.data.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Модель расходов `data` слоя
 *
 * @param id идентификатор операции
 * @param name наименование
 * @param amount кол-во потраченных стредств
 * @param category наименование категории расходов
 * @param time время операции (unix метка, ms)
 */
data class ExpenseEntity(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("amount") val amount: Int,
    @JsonProperty("category") val category: String,
    @JsonProperty("time") val time: Long
)