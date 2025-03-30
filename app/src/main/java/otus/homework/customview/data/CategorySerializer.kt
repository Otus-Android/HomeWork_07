package otus.homework.customview.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import otus.homework.customview.domain.Category

class CategorySerializer: KSerializer<Category> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("customView", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Category {
        return Category.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Category) {
        encoder.encodeString(value.title)
    }
}