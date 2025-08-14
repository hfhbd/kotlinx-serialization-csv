package app.softwork.serialization.flf

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
internal class FixedLengthSealedEncoder(
    private val typeLength: Int?,
    private val originalEncoder: FixedLengthEncoder
) : Encoder by originalEncoder, CompositeEncoder by originalEncoder {
    override val serializersModule: SerializersModule = originalEncoder.serializersModule

    override fun beginStructure(descriptor: SerialDescriptor): FixedLengthSealedEncoder {
        originalEncoder.beginStructure(descriptor)
        return this
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        if (index == 0) {
            if (typeLength != null) {
                originalEncoder.encode(value, typeLength)
            }
        } else {
            originalEncoder.encodeStringElement(descriptor, index, value)
        }
    }
}
