package app.softwork.serialization.csv

import app.softwork.serialization.csv.CSVFormat.NumberFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.native.concurrent.ThreadLocal

/**
 * [RFC-4180](https://datatracker.ietf.org/doc/html/rfc4180)
 */
@ExperimentalSerializationApi
public sealed class CSVFormat(public val configuration: CSVConfiguration) : StringFormat {

    override val serializersModule: SerializersModule get() = configuration.serializersModule

    public enum class NumberFormat {
        Dot, Comma,
    }

    internal class Custom internal constructor(
        configuration: CSVConfiguration
    ) : CSVFormat(configuration)

    @ThreadLocal
    public companion object Default : CSVFormat(CSVConfiguration.default) {
        public operator fun invoke(builder: CSVConfiguration.Builder.() -> Unit): CSVFormat =
            Custom(CSVConfiguration.Builder().apply(builder).build())
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        deserializer.descriptor.checkForLists()
        val parsed = string.parse(configuration.separator, configuration.lineSeparator)

        return if (configuration.includeHeader) {
            val headers = parsed.getHeader()
            val isSequentially = headers.isSequentially(deserializer.descriptor)
            deserializer.deserialize(
                decoder = CSVDecoderImpl(
                    header = headers,
                    nodes = parsed,
                    configuration = configuration,
                    decodesSequentially = isSequentially,
                    level = if (deserializer.descriptor.kind is StructureKind.LIST) -1 else 0,
                )
            )
        } else {
            val decodesSequentially = deserializer.descriptor.kind !is StructureKind.LIST
            deserializer.deserialize(
                decoder = CSVDecoderImpl(
                    header = emptyList(),
                    nodes = parsed,
                    configuration = configuration,
                    decodesSequentially = decodesSequentially,
                    level = if (deserializer.descriptor.kind is StructureKind.LIST) -1 else 0,
                )
            )
        }
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String = buildString {
        serializer.descriptor.checkForLists()

        if (configuration.includeHeader) {
            serializer.descriptor.checkForPolymorphicClasses()
            for (header in serializer.descriptor.flatNames) {
                if (isNotEmpty()) {
                    append(configuration.separator)
                }
                if (configuration.alwaysEmitQuotes) {
                    append('"')
                    append(header.escapeQuotes())
                    append('"')
                } else {
                    append(header)
                }
            }
        }

        serializer.serialize(
            encoder = CSVEncoderImpl(this, configuration),
            value = value
        )
    }
}

@OptIn(ExperimentalSerializationApi::class)
public class CSVConfiguration internal constructor(
    public val separator: Char,
    public val lineSeparator: String,
    public val includeHeader: Boolean,
    public val alwaysEmitQuotes: Boolean,
    public val numberFormat: NumberFormat,
    public val serializersModule: SerializersModule,
) {
    internal companion object {
        internal val default: CSVConfiguration = CSVConfiguration(
            separator = ',',
            lineSeparator = "\n",
            includeHeader = true,
            alwaysEmitQuotes = false,
            numberFormat = NumberFormat.Dot,
            serializersModule = EmptySerializersModule(),
        )
    }

    public class Builder {
        public var separator: Char = default.separator
        public var lineSeparator: String = default.lineSeparator
        public var includeHeader: Boolean = default.includeHeader
        public var alwaysEmitQuotes: Boolean = default.alwaysEmitQuotes
        public var numberFormat: NumberFormat = default.numberFormat
        public var serializersModule: SerializersModule = default.serializersModule

        internal fun build(): CSVConfiguration = CSVConfiguration(
            separator = separator,
            lineSeparator = lineSeparator,
            includeHeader = includeHeader,
            alwaysEmitQuotes = alwaysEmitQuotes,
            numberFormat = numberFormat,
            serializersModule = serializersModule,
        )
    }
}
