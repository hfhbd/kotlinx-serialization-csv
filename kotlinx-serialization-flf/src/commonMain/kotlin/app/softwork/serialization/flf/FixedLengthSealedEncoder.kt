package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*

@ExperimentalSerializationApi
internal class FixedLengthSealedEncoder(
    private val typeLength: Int,
    private val originalEncoder: FixedLengthEncoder
) : Encoder by originalEncoder, CompositeEncoder by originalEncoder {
    override val serializersModule: SerializersModule = originalEncoder.serializersModule

    private var calledType = false

    override fun beginStructure(descriptor: SerialDescriptor): FixedLengthSealedEncoder {
        originalEncoder.beginStructure(descriptor)
        return this
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        if (calledType) {
            originalEncoder.encodeStringElement(descriptor, index, value)
        } else {
            originalEncoder.encode(value, typeLength)
            calledType = true
        }
    }
}
