package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*

@ExperimentalSerializationApi
internal class FixedLengthSealedDecoder(
    private val typeLength: Int,
    private val originalDecoder: FixedLengthDecoder
) : Decoder by originalDecoder, CompositeDecoder by originalDecoder {
    override val serializersModule: SerializersModule = originalDecoder.serializersModule

    private var calledType = false

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        originalDecoder.beginStructure(descriptor)
        return this
    }

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        return if (calledType) {
            originalDecoder.decodeStringElement(descriptor, index)
        } else {
            val value = originalDecoder.decode(typeLength)
            calledType = true
            value
        }
    }
}
