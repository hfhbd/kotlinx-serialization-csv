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
        val data = string.split(lineSeparator)
        return deserializer.deserialize(FixedLengthDecoder(data.iterator(), serializersModule, data.size))
    }

    public fun <T> decodeAsSequence(deserializer: DeserializationStrategy<T>, input: Sequence<String>): Sequence<T> {
        deserializer.descriptor.checkForMaps()
        val iterator = input.iterator()
        return if (iterator.hasNext()) {
            sequence {
                val decoder = FixedLengthDecoder(iterator, serializersModule, size = -1)
                while (iterator.hasNext()) {
                    yield(deserializer.deserialize(decoder))
                }
            }
        } else {
            emptySequence()
        }
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String = buildString {
        serializer.descriptor.checkForMaps()
        serializer.serialize(FixedLengthEncoder(this, serializersModule, lineSeparator), value)
    }

    public fun <T> encodeAsSequence(serializer: SerializationStrategy<T>, value: Sequence<T>): Sequence<String> {
        serializer.descriptor.checkForMaps()
        val iterator = value.iterator()
        return if (iterator.hasNext()) {
            sequence {
                val stringBuilder = StringBuilder()
                val encoder = FixedLengthEncoder(stringBuilder, serializersModule, lineSeparator)
                while (iterator.hasNext()) {
                    serializer.serialize(encoder, iterator.next())
                    yield(stringBuilder.toString())
                    stringBuilder.setLength(0)
                    encoder.afterFirst = false
                }
            }
        } else {
            emptySequence()
        }
    }
}

@ExperimentalSerializationApi
public fun <T> Sequence<T>.encode(
    serializationStrategy: SerializationStrategy<T>,
    format: FixedLengthFormat = FixedLengthFormat
): Sequence<String> = format.encodeAsSequence(serializationStrategy, this)

@ExperimentalSerializationApi
public fun <T> Sequence<String>.decode(
    deserializationStrategy: DeserializationStrategy<T>,
    format: FixedLengthFormat = FixedLengthFormat
): Sequence<T> = format.decodeAsSequence(deserializationStrategy, this)


@ExperimentalSerializationApi
internal fun SerialDescriptor.fixedLength(index: Int) =
    getElementAnnotations(index).filterIsInstance<FixedLength>().singleOrNull()?.length
        ?: error("$serialName.${getElementName(index)} not annotated with @FixedLength")

@ExperimentalSerializationApi
internal val SerialDescriptor.fixedLength
    get() =
        annotations.filterIsInstance<FixedLength>().singleOrNull()?.length
            ?: error("$serialName not annotated with @FixedLength")

@ExperimentalSerializationApi
internal val SerialDescriptor.hasSealedTypeProperty: Boolean
    get() {
        for (index in 0 until elementsCount) {
            if (getElementAnnotations(index).filterIsInstance<FixedLengthSealedClassDiscriminator>().isNotEmpty()) {
                return true
            }
        }
        return false
    }

@ExperimentalSerializationApi
internal val SerialDescriptor.fixedLengthType
    get() =
        annotations.filterIsInstance<FixedLengthSealedClassDiscriminatorLength>().singleOrNull()?.length
            ?: error("$serialName not annotated with @FixedLengthSealedType")

@ExperimentalSerializationApi
internal val SerialDescriptor.fixedLengthList
    get() =
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
