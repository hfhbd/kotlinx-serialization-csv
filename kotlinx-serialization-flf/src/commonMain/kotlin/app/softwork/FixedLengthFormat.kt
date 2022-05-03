package app.softwork

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*

@ExperimentalSerializationApi
@SerialInfo
public annotation class FixedLength(val length: Int)

@ExperimentalSerializationApi
public class FixedLengthFormat(
    override val serializersModule: SerializersModule = EmptySerializersModule
) : StringFormat {
    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        TODO("Not yet implemented")
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String = buildString {
        serializer.serialize(FixedLengthEncoder(this, serializersModule), value)
    }
}

@ExperimentalSerializationApi
internal val SerialDescriptor.fixedLength
    get() = annotations.filterIsInstance<FixedLength>().singleOrNull()
        ?: error("$serialName not annotated with @FixedLength")

@ExperimentalSerializationApi
private class FixedLengthEncoder(
    private val builder: StringBuilder,
    override val serializersModule: SerializersModule
) : FailingPrimitiveEncoder, CompositeEncoder {

    private fun encode(value: String, length: Int) {
        builder.append(value.padEnd(length))
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        encode(value.toString(), descriptor.fixedLength.length)
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        encode(value.toString(), descriptor.fixedLength.length)
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        encode(value.toString(), descriptor.fixedLength.length)
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        encode(value.toString(), descriptor.fixedLength.length)
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        encode(value.toString(), descriptor.fixedLength.length)
    }

    @ExperimentalSerializationApi
    override fun encodeInlineElement(
        descriptor: SerialDescriptor,
        index: Int
    ): Encoder = encodeInline(descriptor.getElementDescriptor(index))

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        encode(value.toString(), descriptor.fixedLength.length)
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        encode(value.toString(), descriptor.fixedLength.length)
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        encodeNullableSerializableValue(serializer, value)
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        encodeSerializableValue(serializer, value)
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        encode(value.toString(), descriptor.fixedLength.length)
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        encode(value, descriptor.fixedLength.length)
    }

    override fun endStructure(descriptor: SerialDescriptor) = Unit

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = this
    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        val value = enumDescriptor.getElementName(index)
        encode(value, enumDescriptor.fixedLength.length)
    }

    @ExperimentalSerializationApi
    override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder =
        FixedLengthPrimitiveEncoder(serializersModule, inlineDescriptor.fixedLength.length, builder)
}

@ExperimentalSerializationApi
internal class FixedLengthPrimitiveEncoder(
    override val serializersModule: SerializersModule,
    private val length: Int,
    private val builder: StringBuilder
) : Encoder {
    private fun encode(value: String, length: Int) {
        builder.append(value.padEnd(length))
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        error("Not supported")
    }

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

    @ExperimentalSerializationApi
    override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder =
        FixedLengthPrimitiveEncoder(serializersModule, inlineDescriptor.fixedLength.length, builder)

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

internal interface FailingPrimitiveEncoder : Encoder {
    override fun encodeBoolean(value: Boolean) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeByte(value: Byte) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeChar(value: Char) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeDouble(value: Double) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeFloat(value: Float) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeInt(value: Int) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeLong(value: Long) {
        error("Primitives are not supported due to missing length")
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeShort(value: Short) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeString(value: String) {
        error("Primitives are not supported due to missing length")
    }
}
