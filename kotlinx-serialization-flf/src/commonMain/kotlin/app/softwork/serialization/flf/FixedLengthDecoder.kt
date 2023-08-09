package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.modules.*

@ExperimentalSerializationApi
public class FixedLengthDecoder(
    override val serializersModule: SerializersModule,
    public val nextRecord: (SerialDescriptor) -> CharSequence,
    public val collectionSize: Int,
) : FailingPrimitiveDecoder {

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val kind = descriptor.kind
        require(kind is StructureKind) {
            "Expected kind is StructureKind, but was $kind."
        }
        return when (kind) {
            StructureKind.CLASS -> {
                val nextValue = nextRecord(descriptor)
                FLFCompositeClassDecoder(
                    serializersModule,
                    value = nextValue
                )
            }
            StructureKind.LIST -> {
                FLFTopLevelListDecoder(
                    serializersModule,
                    collectionSize,
                    nextRecord
                )
            }
            StructureKind.MAP -> error("Not supported")
            StructureKind.OBJECT -> EmptyObjectDecoder
        }
    }

    private var nextInnerListLength = -1

    private val sealedClassClassDiscriminators = mutableMapOf<String, String>()

    internal fun decode(length: Int, trim: Boolean = trimElement): CharSequence {
        val data = decodeElement(length)
        return if (trim) data.trim() else data
    }

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableValue(deserializer: DeserializationStrategy<T?>): T? {
        val isNullabilitySupported = deserializer.descriptor.isNullable
        val length = deserializer.descriptor.fixedLength
        val value = decodeElement(length)
        return if (isNullabilitySupported || value.isNotBlank()) decodeSerializableValue(deserializer) else decodeNull()
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        val fixedLength = enumDescriptor.fixedLength
        return enumDescriptor.elementNames.indexOf(decode(enumDescriptor.fixedLength(fixedLength)))
    }

    override fun decodeInline(descriptor: SerialDescriptor): Decoder {
        val fixedLength = descriptor.fixedLength
        val data = decode(fixedLength)
        return FixedLengthPrimitiveDecoder(serializersModule, data, descriptor.ebcdic)
    }

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Nothing =
        error("Never called, because decodeNullableSerializableValue is overridden.")

    @ExperimentalSerializationApi
    internal fun SerialDescriptor.hasInnerListLengthIndex(): Int? {
        for (element in elementNames) {
            val index = getElementIndex(element)
            val descriptor = getElementDescriptor(index)
            if (descriptor.kind is StructureKind.LIST) {
                for (anno in getElementAnnotations(index)) {
                    if (anno is FixedLengthList) {
                        return getElementIndex(anno.serialName)
                    }
                }
                error("$serialName not annotated with @FixedLengthList")
            }
        }
        return null
    }
}

private class FLFCompositeClassDecoder(
    override val serializersModule: SerializersModule,
    val value: CharSequence
) : CompositeDecoder {
    override fun decodeSequentially(): Boolean = true


    override fun decodeSequentially(): Boolean = true

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        val isInnerClass = level != 0 && deserializer.descriptor.kind is StructureKind.CLASS &&
                !deserializer.descriptor.isInline
        return if (deserializer.descriptor.kind is PolymorphicKind.SEALED) {
            var property: String? = null
            for (anno in descriptor.getElementAnnotations(index)) {
                if (anno is FixedLengthSealedClassDiscriminator) {
                    property = anno.serialName
                    break
                }
            }
            val typeLength = if (property != null) {
                val classDiscriminator = sealedClassClassDiscriminators[property]!!
                SealedClassClassDiscriminator.Property(classDiscriminator)
            } else {
                SealedClassClassDiscriminator.Length(deserializer.descriptor.fixedLengthType)
            }
            deserializer.deserialize(FixedLengthSealedDecoder(typeLength, this))
        } else if (
            descriptor.kind is StructureKind.LIST ||
            deserializer.descriptor.kind is StructureKind.LIST ||
            isInnerClass
        ) {
            deserializer.deserialize(this)
        } else {
            val data = decode(descriptor.fixedLength(index))
            deserializer.deserialize(FixedLengthPrimitiveDecoder(serializersModule, data, descriptor.ebcdic(index)))
        }
    }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short =
        decode(descriptor.fixedLength(index)).toString().toShort()

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        val value = decode(descriptor.fixedLength(index)).toString()
        if (descriptor.hasSealedTypeProperty) {
            sealedClassClassDiscriminators[descriptor.getElementName(index)] = value
        }
        return value
    }

    private var nextLengthElementIndex: Int? = null
    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
        val decoded = decode(descriptor.fixedLength(index))
        val isEbcdic = descriptor.ebcdic(index)?.format

        val int = if (isEbcdic != null) {
            if (decoded.isEmpty()) null else isEbcdic.toInt(decoded)
        } else {
            decoded.toString().toIntOrNull()
        } ?: throw MissingFieldException(descriptor.getElementName(index), descriptor.serialName)

        if (index == nextLengthElementIndex) {
            nextInnerListLength = int
        }
        return int
    }

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double =
        decode(descriptor.fixedLength(index)).toString().toDouble()

    override fun endStructure(descriptor: SerialDescriptor) {
        level -= 1
    }

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean =
        decode(descriptor.fixedLength(index)).toString().toBoolean()

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte =
        decode(descriptor.fixedLength(index)).toString().toByte()

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char =
        decode(descriptor.fixedLength(index), trim = false).single()

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = when (level) {
        0 -> collectionSize
        else -> {
            require(nextInnerListLength != -1) {
                "${descriptor.fixedLengthList} was not seen before this list: $descriptor"
            }
            nextInnerListLength
        }
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float =
        decode(descriptor.fixedLength(index)).toString().toFloat()

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder =
        decodeInline(descriptor.getElementDescriptor(index))

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
        val decoded = decode(descriptor.fixedLength(index))
        return descriptor.ebcdic(index)?.format?.toLong(decoded) ?: decoded.toString().toLong()
    }

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
            data,
            ebcdic = descriptor.ebcdic(index)
        )
        return if (data.isBlank()) {
            decoder.decodeNull()
        } else {
            deserializer.deserialize(decoder)
        }
    }

    private var currentElementIndex = -1
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentElementIndex += 1
        if (descriptor.elementsCount == currentElementIndex) {
            return DECODE_DONE
        }
        return currentElementIndex
    }

}
