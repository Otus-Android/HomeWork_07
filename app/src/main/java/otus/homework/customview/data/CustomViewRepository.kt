package otus.homework.customview.data

import androidx.annotation.RawRes
import javax.inject.Inject

class CustomViewRepository @Inject constructor(private val jsonDataSource: JsonDataSource) {

    fun loadPayloads(@RawRes payloadId: Int): List<Payload> =
        jsonDataSource.loadPayloads(payloadId)
}