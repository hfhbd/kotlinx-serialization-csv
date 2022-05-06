package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*

@ExperimentalSerializationApi
internal class FixedLengthDecoder(
    private val data: List<String>,
    override val serializersModule: SerializersModule
) : FailingPrimitiveDecoder, CompositeDecoder {
    private var level = 0
    private var index = 0
    private var currentRow = 0

    private fun decode(length: Int, trim: Boolean = true): String {
        val data = data[currentRow].substring(index, index + length)
        index += length
        return if (trim) data.trim() else data
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor) =
        error("Never called, because decodeSequentially returns true")

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int) =
        decode(descriptor.fixedLength(index)).toFloat()

    @ExperimentalSerializationApi
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int) =
        decodeInline(descriptor.getElementDescriptor(index))

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int) =
        decode(descriptor.fixedLength(index)).toInt()

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int) =
        decode(descriptor.fixedLength(index)).toLong()

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        val data = decode(descriptor.fixedLength(index))
        val decoder = FixedLengthPrimitiveDecoder(
            serializersModule,
            data
        )
        return if (data.isBlank()) {
            decoder.decodeNull()
        } else {
            deserializer.deserialize(decoder)
        }
    }

    override fun decodeSequentially() = true

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T = if (descriptor.kind is StructureKind.LIST) {
        deserializer.deserialize(this)
    } else {
        val data = decode(descriptor.fixedLength(index))
        deserializer.deserialize(FixedLengthPrimitiveDecoder(serializersModule, data))
    }

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableValue(deserializer: DeserializationStrategy<T?>): T? {
        val isNullabilitySupported = deserializer.descriptor.isNullable
        val length = deserializer.descriptor.fixedLength
        val value = data[currentRow].substring(index, index + length)
        return if (isNullabilitySupported || value.isNotBlank()) decodeSerializableValue(deserializer) else decodeNull()
    }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int) =
        decode(descriptor.fixedLength(index)).toShort()

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int) =
        decode(descriptor.fixedLength(index))

    override fun endStructure(descriptor: SerialDescriptor) {
        level -= 1
        if (level == 0) {
            currentRow += 1
            index = 0
        }
    }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int) =
        decode(descriptor.fixedLength(index)).toBoolean()

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int) =
        decode(descriptor.fixedLength(index)).toByte()

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int) =
        decode(descriptor.fixedLength(index), trim = false).single()

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = data.size

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int) =
        decode(descriptor.fixedLength(index)).toDouble()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        if (descriptor.kind !is StructureKind.LIST) {
            level += 1
        }
        return this
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val fixedLength = enumDescriptor.fixedLength
        return enumDescriptor.elementNames.indexOf(decode(enumDescriptor.fixedLength(fixedLength)))
    }

    @ExperimentalSerializationApi
    override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder {
        val fixedLength = inlineDescriptor.fixedLength
        val data = decode(fixedLength)
        return FixedLengthPrimitiveDecoder(serializersModule, data)
    }

    @ExperimentalSerializationApi
    override fun decodeNotNullMark() =
        error("Never called, because decodeNullableSerializableValue is overridden.")
}
