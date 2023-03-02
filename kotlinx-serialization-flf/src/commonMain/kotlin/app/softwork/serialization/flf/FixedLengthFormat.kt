package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.modules.*
import kotlin.jvm.*
import kotlin.math.*

/**
 * [Fixed Length Files](https://www.ibm.com/docs/en/psfa/latest?topic=format-fixed-length-files)
 */
@ExperimentalSerializationApi
public sealed class FixedLengthFormat(
    override val serializersModule: SerializersModule,
    public val lineSeparator: String,
    internal val fillLeadingZeros: Boolean
) : StringFormat {

    private class Custom(
        serializersModule: SerializersModule,
        lineSeparator: String,
        fillLeadingZeros: Boolean
    ) : FixedLengthFormat(serializersModule, lineSeparator, fillLeadingZeros)

    public companion object Default : FixedLengthFormat(
        serializersModule = EmptySerializersModule(),
        lineSeparator = "\n",
        fillLeadingZeros = true
    ) {
        @JvmOverloads
        public operator fun invoke(
            serializersModule: SerializersModule = EmptySerializersModule(),
            lineSeparator: String = "\n",
            fillLeadingZeros: Boolean = true
        ): FixedLengthFormat = Custom(serializersModule, lineSeparator, fillLeadingZeros)
    }

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        deserializer.descriptor.checkForMaps()
        val data = string.split(lineSeparator)
        var index = 0
        var currentRowIndex = -1
        var currentRow: String? = null
        return deserializer.deserialize(
            FixedLengthDecoder({
                currentRowIndex++
                currentRow = data[currentRowIndex]
                index = 0
            }, { length ->
                currentRow!!.substring(index, min(index + length, currentRow!!.length)).also {
                    index += length
                }
            }, serializersModule, data.size)
        )
    }

    public fun <T> decodeAsSequence(deserializer: DeserializationStrategy<T>, input: Sequence<String>): Sequence<T> {
        deserializer.descriptor.checkForMaps()
        return sequence {
            val iterator = input.iterator()
            if (!iterator.hasNext()) {
                return@sequence
            }
            var currentRow: String? = null
            var index = 0
            val decoder = FixedLengthDecoder({
                currentRow = iterator.next()
                index = 0
            }, { length ->
                val r = currentRow!!.substring(index, min(index + length, currentRow!!.length))
                index += length
                r
            }, serializersModule, size = -1)
            while (iterator.hasNext()) {
                yield(deserializer.deserialize(decoder))
            }
        }
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String = buildString {
        serializer.descriptor.checkForMaps()
        serializer.serialize(FixedLengthEncoder(this, serializersModule, lineSeparator, fillLeadingZeros), value)
    }

    public fun <T> encodeAsSequence(serializer: SerializationStrategy<T>, value: Sequence<T>): Sequence<String> {
        serializer.descriptor.checkForMaps()
        return sequence {
            val iterator = value.iterator()
            if (!iterator.hasNext()) {
                return@sequence
            }

            val stringBuilder = StringBuilder()
            val encoder = FixedLengthEncoder(stringBuilder, serializersModule, lineSeparator = "", fillLeadingZeros)
            while (iterator.hasNext()) {
                serializer.serialize(encoder, iterator.next())
                yield(stringBuilder.toString())
                stringBuilder.setLength(0)
            }
        }
    }
}

@ExperimentalSerializationApi
@JvmOverloads
public fun <T> Sequence<T>.encode(
    serializationStrategy: SerializationStrategy<T>,
    format: FixedLengthFormat = FixedLengthFormat
): Sequence<String> = format.encodeAsSequence(serializationStrategy, this)

@ExperimentalSerializationApi
@JvmOverloads
public fun <T> Sequence<String>.decode(
    deserializationStrategy: DeserializationStrategy<T>,
    format: FixedLengthFormat = FixedLengthFormat
): Sequence<T> = format.decodeAsSequence(deserializationStrategy, this)

@ExperimentalSerializationApi
internal fun SerialDescriptor.fixedLength(index: Int): Int {
    for (anno in getElementAnnotations(index)) {
        if (anno is FixedLength) {
            return anno.length
        }
    }
    error("$serialName.${getElementName(index)} not annotated with @FixedLength")
}

@ExperimentalSerializationApi
internal val SerialDescriptor.fixedLength: Int
    get() {
        for (anno in annotations) {
            if (anno is FixedLength) return anno.length
        }
        error("$serialName not annotated with @FixedLength")
    }

@ExperimentalSerializationApi
internal val SerialDescriptor.hasSealedTypeProperty: Boolean
    get() {
        for (index in 0 until elementsCount) {
            for (anno in getElementAnnotations(index)) {
                if (anno is FixedLengthSealedClassDiscriminator) {
                    return true
                }
            }
        }
        return false
    }

@ExperimentalSerializationApi
internal val SerialDescriptor.fixedLengthType: Int
    get() {
        for (anno in annotations) {
            if (anno is FixedLengthSealedClassDiscriminatorLength) {
                return anno.length
            }
        }
        error("$serialName not annotated with @FixedLengthSealedType")
    }

@ExperimentalSerializationApi
internal val SerialDescriptor.fixedLengthList: String
    get() {
        for (anno in annotations) {
            if (anno is FixedLengthList) {
                return anno.serialName
            }
        }
        error("$serialName not annotated with @FixedLengthList")
    }

@ExperimentalSerializationApi
internal fun SerialDescriptor.checkForMaps() {
    for (descriptor in elementDescriptors) {
        if (descriptor.kind is StructureKind.MAP) {
            error("Map is not yet supported: ${descriptor.serialName}")
        }
        descriptor.checkForMaps()
    }
}

@ExperimentalSerializationApi
internal fun SerialDescriptor.ebcdic(index: Int): Ebcdic? {
    for (anno in getElementAnnotations(index)) {
        if (anno is Ebcdic) {
            return anno
        }
    }
    return null
}

@ExperimentalSerializationApi
internal val SerialDescriptor.ebcdic: Ebcdic?
    get() {
        for (anno in annotations) {
            if (anno is Ebcdic) {
                return anno
            }
        }
        return null
    }
