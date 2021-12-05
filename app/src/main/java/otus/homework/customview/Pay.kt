package otus.homework.customview

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Pay(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "amount") val amount: Int,
    @Json(name = "category") val category: String,
    @Json(name = "time") val time: Long,
)


