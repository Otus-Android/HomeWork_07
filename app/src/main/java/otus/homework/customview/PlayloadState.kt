package otus.homework.customview

import android.os.Parcel
import android.os.Parcelable
import android.view.View

class PlayloadState(superState: Parcelable?) : View.BaseSavedState(superState) {

    var playloads: List<Playload> = listOf()

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeList(playloads)
    }
}
