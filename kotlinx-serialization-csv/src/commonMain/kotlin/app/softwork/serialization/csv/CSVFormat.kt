package app.softwork.serialization.csv

import kotlinx.serialization.*
import kotlinx.serialization.modules.*
import kotlin.jvm.*

/**
 * [RFC-4180](https://datatracker.ietf.org/doc/html/rfc4180)
 */
@ExperimentalSerializationApi
public sealed class CSVFormat(
    private val separator: String,
    private val lineSeparator: String,
    private val includeHeader: Boolean,
    private val alwaysEmitQuotes: Boolean,
    private val numberFormat: NumberFormat,
    override val serializersModule: SerializersModule
) : StringFormat {

    public enum class NumberFormat {
        Dot,
        Comma,
    }

    private class Custom(
        separator: String,
        lineSeparator: String,
        numberFormat: NumberFormat,
        includeHeader: Boolean,
        alwaysEmitQuotes: Boolean,
        serializersModule: SerializersModule,
    ) : CSVFormat(
        separator = separator,
        lineSeparator = lineSeparator,
        numberFormat = numberFormat,
        includeHeader = includeHeader,
        alwaysEmitQuotes = alwaysEmitQuotes,
        serializersModule = serializersModule
    )

    public companion object Default : CSVFormat(
        separator = ",",
        lineSeparator = "\n",
        numberFormat = NumberFormat.Dot,
        includeHeader = true,
        alwaysEmitQuotes = false,
        serializersModule = EmptySerializersModule()
    ) {
        @JvmOverloads
        public operator fun invoke(
            separator: String = ",",
            lineSeparator: String = "\n",
            serializersModule: SerializersModule = EmptySerializersModule(),
            includeHeader: Boolean = true,
            alwaysEmitQuotes: Boolean = false,
            numberFormat: NumberFormat = NumberFormat.Dot,
        ): CSVFormat = Custom(
            separator = separator,
            lineSeparator = lineSeparator,
            numberFormat = numberFormat,
            includeHeader = includeHeader,
            alwaysEmitQuotes = alwaysEmitQuotes,
            serializersModule = serializersModule
        )
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        deserializer.descriptor.checkForLists()
        val lines = string.split(lineSeparator)
        val data = lines.drop(if (includeHeader) 1 else 0).dropLastWhile { it.isEmpty() }.map { it.split(separator) }
        return deserializer.deserialize(
            decoder = CSVDecoder(
                data = data,
                serializersModule = serializersModule,
                numberFormat = numberFormat,
            )
        )
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String = buildString {
        serializer.descriptor.checkForLists()

        if (includeHeader) {
            serializer.descriptor.checkForPolymorphicClasses()
            for (header in serializer.descriptor.flatNames) {
                if (isNotEmpty()) {
                    append(separator)
                }
                append(header)
            }
        }

        serializer.serialize(
            encoder = CSVEncoder(this, separator, lineSeparator, alwaysEmitQuotes, numberFormat, serializersModule),
            value = value
        )
    }
}
