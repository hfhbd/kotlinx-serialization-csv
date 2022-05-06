package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*

@ExperimentalSerializationApi
internal class FixedLengthPrimitiveDecoder(
    override val serializersModule: SerializersModule,
    private val data: String
) : Decoder {
    override fun beginStructure(descriptor: SerialDescriptor) = error("Not supported")

    override fun decodeBoolean() = data.toBoolean()
    override fun decodeByte() = data.toByte()
    override fun decodeChar() = data.single()
    override fun decodeDouble() = data.toDouble()
    override fun decodeFloat() = data.toFloat()
    override fun decodeInt() = data.toInt()
    override fun decodeLong(): Long = data.toLong()
    override fun decodeShort() = data.toShort()
    override fun decodeString() = data

    override fun decodeEnum(enumDescriptor: SerialDescriptor) = enumDescriptor.getElementIndex(data)

    @ExperimentalSerializationApi
    override fun decodeInline(inlineDescriptor: SerialDescriptor) = this

    @ExperimentalSerializationApi
    override fun decodeNotNullMark() = data.isNotBlank()

    @ExperimentalSerializationApi
    override fun decodeNull() = null
}
