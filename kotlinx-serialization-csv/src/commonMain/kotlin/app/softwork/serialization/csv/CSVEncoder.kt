package app.softwork.serialization.csv

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*

@ExperimentalSerializationApi
internal class CSVEncoder(
    private val builder: StringBuilder,
    private val separator: String,
    private val lineSeparator: String,
    override val serializersModule: SerializersModule
) : AbstractEncoder() {
    private var afterFirst = false
    private var level = 0

    override fun encodeValue(value: Any) {
        if (afterFirst) {
            builder.append(separator)
        }
        builder.append(value)
        afterFirst = true
    }

    override fun encodeNull() {
        if (afterFirst) {
            builder.append(separator)
        }
        afterFirst = true
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        if (level == 0) {
            builder.append(lineSeparator)
            afterFirst = false
        }
        level++
        return this
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder = this

    override fun endStructure(descriptor: SerialDescriptor) {
        level--
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        if (level == 0) {
            builder.append(lineSeparator)
            afterFirst = false
        }
        return this
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        if (afterFirst) {
            builder.append(separator)
        }
        builder.append(enumDescriptor.getElementName(index))
    }
}
