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
        deserializer.descriptor.checkForLists()
        return deserializer.deserialize(FixedLengthDecoder(string.split('\n'), serializersModule))
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String = buildString {
        serializer.descriptor.checkForLists()
        serializer.serialize(FixedLengthEncoder(this, serializersModule), value)
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
internal fun SerialDescriptor.checkForLists() {
    for (descriptor in elementDescriptors) {
        if (descriptor.kind is StructureKind.LIST || descriptor.kind is StructureKind.MAP) {
            error("List or Map are not yet supported")
        }
        descriptor.checkForLists()
    }
}
