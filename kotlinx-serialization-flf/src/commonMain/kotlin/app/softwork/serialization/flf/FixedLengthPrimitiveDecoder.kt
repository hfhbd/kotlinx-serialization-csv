package app.softwork.serialization.flf

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
internal class FixedLengthPrimitiveDecoder(
    override val serializersModule: SerializersModule,
    private val data: CharSequence,
    private val ebcdic: Ebcdic?
) : Decoder {
    override fun beginStructure(descriptor: SerialDescriptor) = error("Not supported")

    override fun decodeBoolean() = data.toString().toBoolean()
    override fun decodeByte() = data.toString().toByte()
    override fun decodeChar() = data.single()
    override fun decodeDouble() = data.toString().toDouble()
    override fun decodeFloat() = data.toString().toFloat()
    override fun decodeInt() = ebcdic?.format?.toInt(data) ?: data.toString().toInt()
    override fun decodeLong(): Long = ebcdic?.format?.toLong(data) ?: data.toString().toLong()
    override fun decodeShort() = data.toString().toShort()
    override fun decodeString(): String = data.toString()

    override fun decodeEnum(enumDescriptor: SerialDescriptor) = enumDescriptor.getElementIndex(data.toString())

    override fun decodeInline(descriptor: SerialDescriptor) = this

    @ExperimentalSerializationApi
    override fun decodeNotNullMark() = data.isNotBlank()

    @ExperimentalSerializationApi
    override fun decodeNull() = null
}
