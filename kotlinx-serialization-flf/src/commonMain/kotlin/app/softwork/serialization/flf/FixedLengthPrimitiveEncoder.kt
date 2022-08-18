package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*

@ExperimentalSerializationApi
internal class FixedLengthPrimitiveEncoder(
    override val serializersModule: SerializersModule,
    private val length: Int,
    private val builder: StringBuilder
) : Encoder {
    private fun encode(value: String, length: Int) {
        require(value.length <= length) { "$value was longer as $length" }
        builder.append(value.padEnd(length))
    }

    override fun beginStructure(descriptor: SerialDescriptor) = error("Not supported")

    override fun encodeBoolean(value: Boolean) {
        encode(value.toString(), length)
    }

    override fun encodeByte(value: Byte) {
        encode(value.toString(), length)
    }

    override fun encodeChar(value: Char) {
        encode(value.toString(), length)
    }

    override fun encodeDouble(value: Double) {
        encode(value.toString(), length)
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        val value = enumDescriptor.getElementName(index)
        encode(value, length)
    }

    override fun encodeFloat(value: Float) {
        encode(value.toString(), length)
    }

    override fun encodeInline(descriptor: SerialDescriptor) = this

    override fun encodeInt(value: Int) {
        encode(value.toString(), length)
    }

    override fun encodeLong(value: Long) {
        encode(value.toString(), length)
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        encode("", length)
    }

    override fun encodeShort(value: Short) {
        encode(value.toString(), length)
    }

    override fun encodeString(value: String) {
        encode(value, length)
    }
}
