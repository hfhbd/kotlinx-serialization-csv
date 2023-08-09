package app.softwork.serialization.flf

import kotlinx.serialization.*
import java.io.*
import java.nio.*
import java.nio.charset.*
import java.util.Spliterators.*
import java.util.function.*
import java.util.stream.*
import kotlin.math.min
import kotlin.streams.*

@JvmOverloads
public fun Sequence<String>.appendLines(file: File, charset: Charset = Charsets.UTF_8, lineSeparator: String = "\n") {
    FileOutputStream(file, true).bufferedWriter(charset).use { writer ->
        for (line in this) {
            writer.write(line)
            writer.write(lineSeparator)
        }
    }
}

@JvmOverloads
public fun Sequence<String>.writeLines(file: File, charset: Charset = Charsets.UTF_8, lineSeparator: String = "\n") {
    FileOutputStream(file, false).bufferedWriter(charset).use { writer ->
        for (line in this) {
            writer.write(line)
            writer.write(lineSeparator)
        }
    }
}

@ExperimentalSerializationApi
@JvmOverloads
public fun <T> Readable.decode(
    deserializer: DeserializationStrategy<T>,
    format: FixedLengthFormat = FixedLengthFormat
): Iterable<T> {
    deserializer.descriptor.checkIfList()
    deserializer.descriptor.checkForMaps()

    return Iterable {
        var afterInit = false
        generateSequence {
            try {
                deserializer.deserialize(
                    FixedLengthDecoder({
                        if (afterInit) {
                            val length = format.lineSeparator.length
                            if (length > 0) {
                                if (read(CharBuffer.allocate(length)) != length) {
                                    throw NoMoreDataException()
                                }
                            }
                        }
                        afterInit = true
                    }, decodeElement = { length ->
                        val buffer = CharBuffer.allocate(length)
                        val got = read(buffer)
                        if (got != length) {
                            throw NoMoreDataException()
                        }
                        buffer.position(0)
                        buffer
                    },
                        serializersModule = format.serializersModule,
                        collectionSize = -1,
                        supportsSequentialDecoding = true,
                        trimElement = format.trim,
                        hasNextRow = { true }
                    )
                )
            } catch (_: NoMoreDataException) {
                null
            }
        }.iterator()
    }
}

private class NoMoreDataException : Exception()

@ExperimentalSerializationApi
@JvmOverloads
public fun <T> Stream<String>.decode(
    deserializer: DeserializationStrategy<T>,
    format: FixedLengthFormat = FixedLengthFormat
): Iterable<T> {
    return asSequence().decode(deserializer, format).asIterable()
}

@ExperimentalSerializationApi
@JvmOverloads
public fun <T> Stream<T>.encode(
    serializer: SerializationStrategy<T>,
    format: FixedLengthFormat = FixedLengthFormat
): Iterable<String> {
    return asSequence().encode(serializer, format).asIterable()
}

@ExperimentalSerializationApi
@JvmOverloads
public fun <T> Stream<String>.decodeStream(
    deserializer: DeserializationStrategy<T>,
    format: FixedLengthFormat = FixedLengthFormat
): Stream<T> {
    deserializer.descriptor.checkIfList()
    deserializer.descriptor.checkForMaps()
    val parallel = isParallel
    val split = spliterator()
    return StreamSupport.stream(
        object : AbstractSpliterator<T>(
            split.estimateSize(),
            split.characteristics().and(NONNULL)
        ) {
            var currentRow: String? = null
            var index = 0
            val decoder = FixedLengthDecoder(
                nextRow = {
                    index = 0
                },
                decodeElement = {
                    val max = min(index + it, currentRow!!.length)
                    currentRow!!.subSequence(index, max)
                },
                serializersModule = format.serializersModule,
                collectionSize = exactSizeIfKnown.toIntOrMinusOne(),
                supportsSequentialDecoding = true,
                hasNextRow = { true },
                trimElement = format.trim
            )

            override fun tryAdvance(action: Consumer<in T>): Boolean {
                return split.tryAdvance {
                    currentRow = it
                    val t = deserializer.deserialize(decoder)
                    action.accept(t)
                }
            }

            fun Long.toIntOrMinusOne(): Int {
                return if (this < Int.MIN_VALUE || this > Int.MAX_VALUE) {
                    -1
                } else {
                    this.toInt()
                }
            }
        },
        parallel
    ).onClose(::close)
}

@ExperimentalSerializationApi
@JvmOverloads
public fun <T> Stream<T>.encodeStream(
    serializer: SerializationStrategy<T>,
    format: FixedLengthFormat = FixedLengthFormat
): Stream<String> {
    serializer.descriptor.checkForMaps()
    val parallel = isParallel
    val split = spliterator()
    return StreamSupport.stream(
        object : AbstractSpliterator<String>(
            split.estimateSize(),
            split.characteristics().and(NONNULL)
        ) {
            val currentRow = StringBuilder()
            val encoder = FixedLengthEncoder(
                currentRow,
                format.serializersModule,
                lineSeparator = "",
                format.fillLeadingZeros
            )

            override fun tryAdvance(action: Consumer<in String>): Boolean {
                return split.tryAdvance {
                    serializer.serialize(encoder, it)
                    action.accept(currentRow.toString())
                    currentRow.setLength(0)
                }
            }
        },
        parallel
    ).onClose(::close)
}

@ExperimentalSerializationApi
@JvmOverloads
public fun <T> Appendable.append(
    serializer: SerializationStrategy<T>,
    value: T,
    format: FixedLengthFormat = FixedLengthFormat
) {
    append(format.encodeToString(serializer, value))
}

@ExperimentalSerializationApi
@JvmOverloads
public fun <T> Appendable.appendLine(
    serializer: SerializationStrategy<T>,
    value: T,
    format: FixedLengthFormat = FixedLengthFormat
) {
    appendLine(format.encodeToString(serializer, value))
}
