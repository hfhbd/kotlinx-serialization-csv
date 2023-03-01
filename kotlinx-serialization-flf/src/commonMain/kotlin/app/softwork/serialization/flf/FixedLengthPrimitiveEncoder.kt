package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*

@ExperimentalSerializationApi
internal class FixedLengthPrimitiveEncoder(
    override val serializersModule: SerializersModule,
    private val length: Int,
    private val builder: StringBuilder,
    private val fillLeadingZero: Boolean,
    private val ebcdic: Ebcdic?
) : Encoder {
    private fun encode(value: String, length: Int) {
        require(value.length <= length) { "$value was longer as $length" }
        builder.append(value.padEnd(length))
    }
    private fun encodeNumber(value: String, length: Int) {
        if (fillLeadingZero) {
            val sign = value.startsWith("-")
            if (sign) {
                encode("-" + value.drop(1).padStart(length -1, '0'), length)
            } else {
                encode(value.padStart(length, '0'), length)
            }
        } else {
            encode(value, length)
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor) = error("Not supported")

    override fun encodeBoolean(value: Boolean) {
        encode(value.toString(), length)
    }

    override fun encodeByte(value: Byte) {
        encodeNumber(value.toString(), length)
    }

    override fun encodeChar(value: Char) {
        encode(value.toString(), length)
    }

    override fun encodeDouble(value: Double) {
        encodeNumber(value.toString(), length)
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        val value = enumDescriptor.getElementName(index)
        encode(value, length)
    }

    override fun encodeFloat(value: Float) {
        encodeNumber(value.toString(), length)
    }

    override fun encodeInline(descriptor: SerialDescriptor) = this

    override fun encodeInt(value: Int) {
        val stringValue = ebcdic?.format?.toString(value) ?: value.toString()
        encodeNumber(stringValue, length)
    }

    override fun encodeLong(value: Long) {
        val stringValue = ebcdic?.format?.toString(value) ?: value.toString()
        encodeNumber(stringValue, length)
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        encode("", length)
    }

    override fun encodeShort(value: Short) {
        encodeNumber(value.toString(), length)
    }

    override fun encodeString(value: String) {
        encode(value, length)
    }
}
