package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.modules.*

/**
 * [Fixed Length Files](https://www.ibm.com/docs/en/psfa/latest?topic=format-fixed-length-files)
 */
@ExperimentalSerializationApi
public sealed class FixedLengthFormat(
    override val serializersModule: SerializersModule
) : StringFormat {

    private class Custom(serializersModule: SerializersModule) : FixedLengthFormat(serializersModule)

    public companion object Default : FixedLengthFormat(EmptySerializersModule) {
        public operator fun invoke(serializersModule: SerializersModule): FixedLengthFormat = Custom(serializersModule)
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        TODO("Not yet implemented")
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String = buildString {
        serializer.serialize(FixedLengthEncoder(this, serializersModule), value)
    }
}

@ExperimentalSerializationApi
internal fun SerialDescriptor.fixedLength(index: Int) =
    getElementAnnotations(index).filterIsInstance<FixedLength>().singleOrNull()?.length
        ?: error("${getElementName(index)} not annotated with @FixedLength")
