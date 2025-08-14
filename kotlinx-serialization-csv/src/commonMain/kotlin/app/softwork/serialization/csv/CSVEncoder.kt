package app.softwork.serialization.csv

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule

public interface CSVEncoder : Encoder, CompositeEncoder {
    public val configuration: CSVConfiguration
}

@ExperimentalSerializationApi
internal class CSVEncoderImpl(
    private val builder: StringBuilder,
    override val configuration: CSVConfiguration,
) : AbstractEncoder(), CSVEncoder {
    override val serializersModule: SerializersModule
        get() = configuration.serializersModule

    private var afterFirst = false
    private var level = 0

    override fun encodeValue(value: Any) {
        if (afterFirst) {
            builder.append(configuration.separator)
        }
        val valueToAppend = value.toString()
        val quote =
            configuration.alwaysEmitQuotes || configuration.separator in valueToAppend || configuration.lineSeparator in valueToAppend
        if (quote) {
            builder.append('"')
            builder.append(valueToAppend.escapeQuotes())
            builder.append('"')
        } else {
            builder.append(valueToAppend)
        }
        afterFirst = true
    }

    override fun encodeDouble(value: Double) {
        encodeNumber(value)
    }

    override fun encodeFloat(value: Float) {
        encodeNumber(value)
    }

    private fun encodeNumber(value: Number) {
        when (configuration.numberFormat) {
            CSVFormat.NumberFormat.Dot -> encodeValue(value)
            CSVFormat.NumberFormat.Comma -> encodeValue(value.toString().replace(".", ","))
        }
    }

    override fun encodeNull() {
        if (afterFirst) {
            builder.append(configuration.separator)
        }
        afterFirst = true
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        if (level == 0) {
            if (builder.isNotEmpty()) {
                builder.append(configuration.lineSeparator)
            }
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
            builder.append(configuration.lineSeparator)
            afterFirst = false
        }
        return this
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeValue(enumDescriptor.getElementName(index))
    }
}

internal fun String.escapeQuotes(): String = replace("\"", "\"\"")
