package otus.homework.customview.data.models

import com.fasterxml.jackson.annotation.JsonProperty

data class ExpenseEntity(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("amount") val amount: Int,
    @JsonProperty("category") val category: String,
    @JsonProperty("time") val time: Long
)