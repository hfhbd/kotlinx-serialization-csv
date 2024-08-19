package app.softwork.serialization.csv

import app.softwork.serialization.csv.CSVFormat.NumberFormat
import kotlinx.serialization.*
import kotlinx.serialization.modules.*
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

    public companion object Default : CSVFormat(CSVConfiguration.default) {
        public operator fun invoke(builder: CSVConfiguration.Builder.() -> Unit): CSVFormat =
            Custom(CSVConfiguration.Builder().apply(builder).build())
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        deserializer.descriptor.checkForLists()
        val lines = string.split(configuration.lineSeparator)
        val data = lines.drop(if (configuration.includeHeader) 1 else 0).dropLastWhile { it.isEmpty() }
            .map { it.split(configuration.separator) }
        return deserializer.deserialize(
            decoder = CSVDecoder(
                data = data,
                configuration = configuration,
            )
        )
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String = buildString {
        serializer.descriptor.checkForLists()

        if (configuration.includeHeader) {
            serializer.descriptor.checkForPolymorphicClasses()
            for (header in serializer.descriptor.flatNames) {
                if (isNotEmpty()) {
                    append(configuration.separator)
                }
                append(header)
            }
        }

        serializer.serialize(
            encoder = CSVEncoder(this, configuration),
            value = value
        )
    }
}

@OptIn(ExperimentalSerializationApi::class)
public class CSVConfiguration internal constructor(
    public val separator: String,
    public val lineSeparator: String,
    public val includeHeader: Boolean,
    public val alwaysEmitQuotes: Boolean,
    public val numberFormat: NumberFormat,
    public val serializersModule: SerializersModule,
) {
    public companion object {
        @ThreadLocal
        public val default: CSVConfiguration = CSVConfiguration(
            separator = ",",
            lineSeparator = "\n",
            includeHeader = true,
            alwaysEmitQuotes = false,
            numberFormat = NumberFormat.Dot,
            serializersModule = EmptySerializersModule(),
        )
    }

    public class Builder {
        public var separator: String = default.separator
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
