package otus.homework.customview.model

import kotlinx.serialization.Serializable

@Serializable
data class ListStore(
    val stores : List<Store>
)
