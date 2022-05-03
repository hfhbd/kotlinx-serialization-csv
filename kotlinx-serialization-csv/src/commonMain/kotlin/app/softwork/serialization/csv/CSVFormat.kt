package app.softwork.serialization.csv

import kotlinx.serialization.*
import kotlinx.serialization.modules.*

/**
 * [RFC-4180](https://datatracker.ietf.org/doc/html/rfc4180)
 */
@ExperimentalSerializationApi
public sealed class CSVFormat(
    private val separator: String,
    override val serializersModule: SerializersModule
) : StringFormat {
    private class Custom(separator: String, serializersModule: SerializersModule) :
        CSVFormat(separator, serializersModule)

    public companion object Default : CSVFormat(
        ",", EmptySerializersModule
    ) {
        public operator fun invoke(separator: String, serializersModule: SerializersModule): CSVFormat =
            Custom(separator, serializersModule)
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        val lines = string.split('\n')
        val data = lines.drop(1).map { it.split(separator) }
        return deserializer.deserialize(
            decoder = CSVDecoder(
                data = data,
                maxIndex = Iterable { deserializer.descriptor.flatNames }.count(),
                serializersModule = serializersModule
            )
        )
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String = buildString {
        var afterFirst = false

        serializer.descriptor.flatNames.forEach {
            if (afterFirst) {
                append(separator)
            }
            append(it)
            afterFirst = true
        }

        serializer.serialize(
            encoder = CSVEncoder(this, separator, serializersModule),
            value = value
        )
    }
}
