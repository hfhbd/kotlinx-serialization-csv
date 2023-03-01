package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*

@ExperimentalSerializationApi
internal class FixedLengthEncoder(
    private val builder: StringBuilder,
    override val serializersModule: SerializersModule,
    private val lineSeparator: String,
    private val fillLeadingZero: Boolean
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

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        encode(value.toString(), descriptor.fixedLength(index))
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        encodeNumber(value.toString(), descriptor.fixedLength(index))
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        encode(value.toString(), descriptor.fixedLength(index))
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        encodeNumber(value.toString(), descriptor.fixedLength(index))
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        encodeNumber(value.toString(), descriptor.fixedLength(index))
    }

    override fun encodeInlineElement(
        descriptor: SerialDescriptor,
        index: Int
    ): Encoder = encodeInline(descriptor.getElementDescriptor(index))

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        val stringValue = descriptor.ebcdic(index)?.format?.toString(value) ?: value.toString()
        encodeNumber(stringValue, descriptor.fixedLength(index))
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        val stringValue = descriptor.ebcdic(index)?.format?.toString(value) ?: value.toString()
        encodeNumber(stringValue, descriptor.fixedLength(index))
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
            builder,
            fillLeadingZero,
            descriptor.ebcdic(index)
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
            var length: Int? = serializer.descriptor.fixedLengthType
            for (anno in descriptor.getElementAnnotations(index)) {
                if (anno is FixedLengthSealedClassDiscriminator) {
                    length = null
                    break
                }
            }
            serializer.serialize(FixedLengthSealedEncoder(length, this), value)
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
                    builder,
                    fillLeadingZero,
                    descriptor.ebcdic(index)
                ),
                value
            )
        }
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        encodeNumber(value.toString(), descriptor.fixedLength(index))
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
        return FixedLengthPrimitiveEncoder(serializersModule, fixedLength, builder, fillLeadingZero, descriptor.ebcdic)
    }
}
