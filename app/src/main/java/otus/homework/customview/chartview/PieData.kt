package otus.homework.customview.chartview

import android.os.Parcel
import android.os.Parcelable
import otus.homework.customview.utils.createPaint

class PieData(parcel: Parcel? = null) : Parcelable {

    var pieModels: HashMap<String, PieModel> = hashMapOf()
    var currencyLabel: String = "$"
        private set

    fun setNewCurrencyLabel(newCurrencyLabel: String) {
        currencyLabel = newCurrencyLabel
    }

    var totalAmount = 0.0

    fun add(
        name: String,
        amount: Double,
        color: String? = null
    ) {
        if (pieModels.containsKey(name)) {
            pieModels[name]?.let { pieModel ->
                pieModel.amount += amount
            }
        } else {
            pieModels[name] = PieModel(
                name = name,
                amount = amount,
                paint = createPaint(color, pieModels.size)
            )
        }
        totalAmount += amount
    }

    companion object CREATOR: Parcelable.Creator<PieData> {
        override fun newArray(size: Int): Array<PieData?> {
            return arrayOfNulls(size)
        }

        override fun createFromParcel(source: Parcel): PieData {
            return PieData(source)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(currencyLabel)
        dest.writeMap(pieModels)
        dest.writeDouble(totalAmount)
    }

    init {
        currencyLabel = parcel?.readString().orEmpty()
        parcel?.let {
          pieModels = it.readHashMap(null) as HashMap<String, PieModel>
        }
        totalAmount = parcel?.readDouble() ?: 0.0
    }
}