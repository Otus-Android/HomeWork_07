package otus.homework.customview.model

import kotlinx.serialization.Serializable

@Serializable
data class Store(
    val id : Int,
    val name : String,
    val amount : Int,
    val category : String,
    val time : Int,
    var beginDegree : Int = 0,
    var endDegree : Int = 0,
    var isSelect : Boolean = false,
)
