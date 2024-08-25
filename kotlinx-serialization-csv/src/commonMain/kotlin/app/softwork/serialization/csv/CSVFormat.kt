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
    private val encodeHeader: Boolean,
    private val alwaysEmitQuotes: Boolean,
    override val serializersModule: SerializersModule
) : StringFormat {
    private class Custom(
        separator: String,
        lineSeparator: String,
        encodeHeader: Boolean,
        alwaysEmitQuotes: Boolean,
        serializersModule: SerializersModule,
    ) : CSVFormat(
        separator = separator,
        lineSeparator = lineSeparator,
        encodeHeader = encodeHeader,
        alwaysEmitQuotes = alwaysEmitQuotes,
        serializersModule = serializersModule
    )

    public companion object Default : CSVFormat(
        separator = ",",
        lineSeparator = "\n",
        encodeHeader = true,
        alwaysEmitQuotes = false,
        serializersModule = EmptySerializersModule()
    ) {
        @JvmOverloads
        public operator fun invoke(
            separator: String = ",",
            lineSeparator: String = "\n",
            encodeHeader: Boolean = true,
            alwaysEmitQuotes: Boolean = false,
            serializersModule: SerializersModule = EmptySerializersModule()
        ): CSVFormat =
            Custom(
                separator = separator,
                lineSeparator = lineSeparator,
                encodeHeader = encodeHeader,
                alwaysEmitQuotes = alwaysEmitQuotes,
                serializersModule = serializersModule
            )
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        deserializer.descriptor.checkForLists()
        val lines = string.split(lineSeparator)
        val data = lines.drop(1).dropLastWhile { it.isEmpty() }.map { it.split(separator) }
        return deserializer.deserialize(
            decoder = CSVDecoder(
                data = data,
                serializersModule = serializersModule
            )
        )
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String = buildString {
        serializer.descriptor.checkForLists()

        if (encodeHeader) {
            serializer.descriptor.checkForPolymorphicClasses()
            for (header in serializer.descriptor.flatNames) {
                if (isNotEmpty()) {
                    append(separator)
                }
                append(header)
            }
        }

        serializer.serialize(
            encoder = CSVEncoder(this, separator, lineSeparator, alwaysEmitQuotes, serializersModule),
            value = value
        )
    }
}
