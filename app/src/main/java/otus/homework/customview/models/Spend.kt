package otus.homework.customview.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Spend(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("category")
    val category: String,
    @SerializedName("time")
    val time: Int
): Parcelable