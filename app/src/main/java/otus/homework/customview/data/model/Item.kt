package otus.homework.customview.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Item(
  val id: Int,
  val name: String,
  var amount: Int,
  val category: String,
  val time: Long
)