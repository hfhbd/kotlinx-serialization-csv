package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*

@ExperimentalSerializationApi
internal class FixedLengthEncoder(
    private val builder: StringBuilder,
    override val serializersModule: SerializersModule,
    private val lineSeparator: String
) : FailingPrimitiveEncoder, CompositeEncoder {

    private var level = 0
    private var afterFirst = false

    private fun maybeAddLine() {
        if (level == 0 && afterFirst) {
            builder.append(lineSeparator)
        }
    }

    internal fun encode(value: String, length: Int) {
        require(value.length <= length) { "$value was longer as $length" }
        builder.append(value.padEnd(length))
        afterFirst = true
    }

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        encode(value.toString(), descriptor.fixedLength(index))
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        encode(value.toString(), descriptor.fixedLength(index))
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        encode(value.toString(), descriptor.fixedLength(index))
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        encode(value.toString(), descriptor.fixedLength(index))
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        encode(value.toString(), descriptor.fixedLength(index))
    }

    override fun encodeInlineElement(
        descriptor: SerialDescriptor,
        index: Int
    ): Encoder = encodeInline(descriptor.getElementDescriptor(index))

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        encode(value.toString(), descriptor.fixedLength(index))
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        encode(value.toString(), descriptor.fixedLength(index))
    }

    @ExperimentalSerializationApi
    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        val encoder = FixedLengthPrimitiveEncoder(
            serializersModule,
            descriptor.fixedLength(index),
            builder
        )
        if (value == null) {
            encoder.encodeNull()
        } else {
            serializer.serialize(encoder, value)
        }
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        val isInnerClass = level != 0 && serializer.descriptor.kind is StructureKind.CLASS &&
            !serializer.descriptor.isInline
        if (serializer.descriptor.kind is PolymorphicKind.SEALED) {
            val length = if (descriptor.getElementAnnotations(index)
                .filterIsInstance<FixedLengthSealedClassDiscriminator>()
                .isNotEmpty()
            ) null else serializer.descriptor.fixedLengthType
            serializer.serialize(
                FixedLengthSealedEncoder(
                    length,
                    this
                ),
                value
            )
        } else if (
            descriptor.kind is StructureKind.LIST ||
            serializer.descriptor.kind is StructureKind.LIST ||
            isInnerClass
        ) {
            serializer.serialize(this, value)
        } else {
            serializer.serialize(
                FixedLengthPrimitiveEncoder(
                    serializersModule,
                    descriptor.fixedLength(index),
                    builder
                ),
                value
            )
        }
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        encode(value.toString(), descriptor.fixedLength(index))
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        encode(value, descriptor.fixedLength(index))
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        maybeAddLine()
        level++
        return this
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder = this
    override fun endStructure(descriptor: SerialDescriptor) {
        level--
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        val value = enumDescriptor.getElementName(index)
        encode(value, enumDescriptor.fixedLength(index))
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        maybeAddLine()
        val fixedLength = descriptor.fixedLength
        return FixedLengthPrimitiveEncoder(serializersModule, fixedLength, builder)
    }
}
