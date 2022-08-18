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

    private var lengthIndex: Int? = null
    private var currentLength: Int? = null

    internal fun decode(length: Int, trim: Boolean = true): String {
        val data = data[currentRow].substring(index, index + length)
        index += length
        return if (trim) data.trim() else data
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor) =
        error("Never called, because decodeSequentially returns true")

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int) =
        decode(descriptor.fixedLength(index)).toFloat()

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int) =
        decodeInline(descriptor.getElementDescriptor(index))

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int) =
        decode(descriptor.fixedLength(index)).toInt().also {
            if (index == lengthIndex) {
                currentLength = it
            }
        }

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
    ): T {
        val isInnerClass = level != 0 && deserializer.descriptor.kind is StructureKind.CLASS &&
            !deserializer.descriptor.isInline
        return if (deserializer.descriptor.kind is PolymorphicKind.SEALED) {
            val length = deserializer.descriptor.fixedLengthType
            deserializer.deserialize(FixedLengthSealedDecoder(length, this))
        } else if (descriptor.kind is PolymorphicKind.SEALED && index == 1) {
            deserializer.deserialize(this)
        } else if (
            descriptor.kind is StructureKind.LIST ||
            deserializer.descriptor.kind is StructureKind.LIST ||
            isInnerClass
        ) {
            deserializer.deserialize(this)
        } else {
            val data = decode(descriptor.fixedLength(index))
            deserializer.deserialize(FixedLengthPrimitiveDecoder(serializersModule, data))
        }
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

    override fun decodeCollectionSize(descriptor: SerialDescriptor) = when (level) {
        0 -> data.size
        else ->
            currentLength ?: error("${descriptor.fixedLengthList} was not seen before this list: $descriptor")
    }

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int) =
        decode(descriptor.fixedLength(index)).toDouble()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val hasInnerList = descriptor.hasInnerListLengthIndex()
        if (hasInnerList != null) {
            lengthIndex = hasInnerList
        }
        if (descriptor.kind !is StructureKind.LIST) {
            level += 1
        }
        return this
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val fixedLength = enumDescriptor.fixedLength
        return enumDescriptor.elementNames.indexOf(decode(enumDescriptor.fixedLength(fixedLength)))
    }

    override fun decodeInline(descriptor: SerialDescriptor): Decoder {
        val fixedLength = descriptor.fixedLength
        val data = decode(fixedLength)
        return FixedLengthPrimitiveDecoder(serializersModule, data)
    }

    @ExperimentalSerializationApi
    override fun decodeNotNullMark() =
        error("Never called, because decodeNullableSerializableValue is overridden.")

    @ExperimentalSerializationApi
    internal fun SerialDescriptor.hasInnerListLengthIndex(): Int? {
        for (element in elementNames) {
            val index = getElementIndex(element)
            val descriptor = getElementDescriptor(index)
            if (descriptor.kind is StructureKind.LIST) {
                val lengthName = getElementAnnotations(index).filterIsInstance<FixedLengthList>().singleOrNull()
                    ?: error("$serialName not annotated with @FixedLengthList")
                return getElementIndex(lengthName.serialName)
            }
        }
        return null
    }
}
