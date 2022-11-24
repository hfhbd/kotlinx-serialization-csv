package app.softwork.serialization.csv

import kotlinx.serialization.*
import kotlinx.serialization.modules.*

/**
 * [RFC-4180](https://datatracker.ietf.org/doc/html/rfc4180)
 */
@ExperimentalSerializationApi
public sealed class CSVFormat(
    private val separator: String,
    private val lineSeparator: String,
    override val serializersModule: SerializersModule
) : StringFormat {
    private class Custom(
        separator: String,
        lineSeparator: String,
        serializersModule: SerializersModule
    ) : CSVFormat(separator, lineSeparator, serializersModule)

    public companion object Default : CSVFormat(
        separator = ",",
        lineSeparator = "\n",
        serializersModule = EmptySerializersModule()
    ) {
        public operator fun invoke(
            separator: String = ",",
            lineSeparator: String = "\n",
            serializersModule: SerializersModule = EmptySerializersModule()
        ): CSVFormat =
            Custom(separator, lineSeparator, serializersModule)
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        deserializer.descriptor.checkForLists()
        val lines = string.split(lineSeparator)
        val data = lines.drop(1).map { it.split(separator) }
        return deserializer.deserialize(
            decoder = CSVDecoder(
                data = data,
                serializersModule = serializersModule
            )
        )
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String = buildString {
        serializer.descriptor.checkForLists()
        var afterFirst = false

        serializer.descriptor.flatNames.forEach {
            if (afterFirst) {
                append(separator)
            }
            append(it)
            afterFirst = true
        }

        serializer.serialize(
            encoder = CSVEncoder(this, separator, lineSeparator, serializersModule),
            value = value
        )
    }
}
