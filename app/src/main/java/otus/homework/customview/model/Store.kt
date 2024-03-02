package otus.homework.customview.model

import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id : Int,
    val name : String,
    val amount : Int,
    val category : String,
    val time : Int,
)
