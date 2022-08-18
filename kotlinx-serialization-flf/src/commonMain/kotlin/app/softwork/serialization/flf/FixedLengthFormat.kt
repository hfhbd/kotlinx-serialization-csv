package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.modules.*

/**
 * [Fixed Length Files](https://www.ibm.com/docs/en/psfa/latest?topic=format-fixed-length-files)
 */
@ExperimentalSerializationApi
public sealed class FixedLengthFormat(
    override val serializersModule: SerializersModule,
    private val lineSeparator: String
) : StringFormat {

    private class Custom(serializersModule: SerializersModule, lineSeparator: String) :
        FixedLengthFormat(serializersModule, lineSeparator)

    public companion object Default : FixedLengthFormat(EmptySerializersModule(), lineSeparator = "\n") {
        public operator fun invoke(
            serializersModule: SerializersModule,
            lineSeparator: String = "\n"
        ): FixedLengthFormat = Custom(serializersModule, lineSeparator)
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        deserializer.descriptor.checkForMaps()
        return deserializer.deserialize(FixedLengthDecoder(string.split(lineSeparator), serializersModule))
    }

    public fun <T> decodeAsSequence(deserializer: DeserializationStrategy<T>, input: Sequence<String>): Sequence<T> {
        deserializer.descriptor.checkForMaps()
        return input.map {
            deserializer.deserialize(FixedLengthDecoder(listOf(it), serializersModule))
        }
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String = buildString {
        serializer.descriptor.checkForMaps()
        serializer.serialize(FixedLengthEncoder(this, serializersModule, lineSeparator), value)
    }

    public fun <T> encodeAsSequence(serializer: SerializationStrategy<T>, value: Sequence<T>): Sequence<String> {
        serializer.descriptor.checkForMaps()
        return value.map {
            buildString {
                serializer.serialize(FixedLengthEncoder(this, serializersModule, lineSeparator), it)
            }
        }
    }
}

@ExperimentalSerializationApi
internal fun SerialDescriptor.fixedLength(index: Int) =
    getElementAnnotations(index).filterIsInstance<FixedLength>().singleOrNull()?.length
        ?: error("$serialName.${getElementName(index)} not annotated with @FixedLength")

@ExperimentalSerializationApi
internal val SerialDescriptor.fixedLength get() =
    annotations.filterIsInstance<FixedLength>().singleOrNull()?.length
        ?: error("$serialName not annotated with @FixedLength")

@ExperimentalSerializationApi
internal val SerialDescriptor.fixedLengthType get() =
    annotations.filterIsInstance<FixedLengthSealedType>().singleOrNull()?.length
        ?: error("$serialName not annotated with @FixedLengthSealedType")

@ExperimentalSerializationApi
internal val SerialDescriptor.fixedLengthList get() =
    annotations.filterIsInstance<FixedLengthList>().singleOrNull()?.serialName
        ?: error("$serialName not annotated with @FixedLengthList")

@ExperimentalSerializationApi
internal fun SerialDescriptor.checkForMaps() {
    for (descriptor in elementDescriptors) {
        if (descriptor.kind is StructureKind.MAP) {
            error("Map are not yet supported: ${descriptor.serialName}")
        }
        descriptor.checkForMaps()
    }
}
