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
    internal val trim: Boolean = true,
    internal val fillLeadingZeros: Boolean
) : StringFormat {

    private class Custom(
        serializersModule: SerializersModule,
        lineSeparator: String,
        trim: Boolean,
        fillLeadingZeros: Boolean
    ) : FixedLengthFormat(serializersModule, lineSeparator, trim, fillLeadingZeros)

    public data object MemoryFormat: FixedLengthFormat(
        serializersModule = EmptySerializersModule(),
        lineSeparator = "",
        trim = true,
        fillLeadingZeros = true
    )

    public companion object Default : FixedLengthFormat(
        serializersModule = EmptySerializersModule(),
        lineSeparator = "\n",
        trim = true,
        fillLeadingZeros = true
    ) {
        @JvmOverloads
        public operator fun invoke(
            lineSeparator: String = "\n",
            fillLeadingZeros: Boolean = true,
            trim: Boolean = true,
            serializersModule: SerializersModule = EmptySerializersModule(),
        ): FixedLengthFormat = Custom(serializersModule, lineSeparator, trim, fillLeadingZeros)
    }

    public fun <T> decodeFromCharSequence(deserializer: DeserializationStrategy<T>, charSequence: CharSequence): T =
        if (lineSeparator == "") {
            deserializer.descriptor.checkForMaps()

            var index = 0
            deserializer.deserialize(
                FixedLengthDecoder(
                    nextRow = {
                        // empty lineSeparator results into 1 row
                    },
                    decodeElement = { length ->
                        val maxLength = min(index + length, charSequence.length)
                        val next = charSequence.subSequence(index, maxLength)
                        index += length
                        next
                    },
                    hasNextRow = { descriptor ->
                        val minLength = descriptor.parseRecordMinLength(charSequence)
                        val maxLength = min(index + minLength, charSequence.length)
                        minLength <= maxLength
                    },
                    serializersModule = serializersModule,
                    collectionSize = -1,
                    supportsSequentialDecoding = false, // "" does not have a record delimiter, so the size of all records is unknown.
                    trimElement = trim,
                )
            )
        } else {
            deserializer.descriptor.checkForMaps()
            val data = charSequence.split(lineSeparator)
            var index = 0
            var currentRowIndex = -1
            var currentRow: String? = null
            deserializer.deserialize(
                FixedLengthDecoder(
                    nextRow = {
                        currentRowIndex++
                        currentRow = data[currentRowIndex]
                        index = 0
                    },
                    decodeElement = { length ->
                        val maxLength = min(index + length, currentRow!!.length)
                        val next = currentRow!!.substring(index, maxLength)
                        index += length
                        next
                    }, 
                    hasNextRow = {
                      true // not called due supportsSequentialDecoding = true             
                    },
                    serializersModule = serializersModule,
                    collectionSize = data.size,
                    supportsSequentialDecoding = true,
                    trimElement = trim,
                )
            )
        }
    
    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        return decodeFromCharSequence(deserializer, string)
    }

    public fun <T> decodeAsSequence(deserializer: DeserializationStrategy<T>, input: Sequence<String>): Sequence<T> {
        deserializer.descriptor.checkIfList()
        deserializer.descriptor.checkForMaps()
        return sequence {
            val iterator = input.iterator()
            if (!iterator.hasNext()) {
                return@sequence
            }
            var currentRow: String? = null
            var index = 0
            val decoder = FixedLengthDecoder(
                nextRow = {
                    currentRow = iterator.next()
                    index = 0
                },
                decodeElement = { length ->
                    val maxLength = min(index + length, currentRow!!.length)
                    val next = currentRow!!.subSequence(index, maxLength)
                    index += length
                    next
                },
                hasNextRow = { true },
                serializersModule = serializersModule,
                collectionSize = -1,
                supportsSequentialDecoding = true,
                trimElement = trim,
            )
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

    public inline fun <reified In, reified Out> convert(
        value: In,
        from: SerializationStrategy<In> = serializer<In>(),
        to: DeserializationStrategy<Out> = serializer<Out>()
    ): Out = decodeFromString(to, encodeToString(from, value))
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
public fun SerialDescriptor.checkForMaps() {
    for (descriptor in elementDescriptors) {
        if (descriptor.kind is StructureKind.MAP) {
            error("Map is not yet supported: ${descriptor.serialName}")
        }
        descriptor.checkForMaps()
    }
}

@ExperimentalSerializationApi
public fun SerialDescriptor.checkIfList() {
    require(kind !is StructureKind.LIST) {
        "Top level list is not supported for lazy decoding."
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
