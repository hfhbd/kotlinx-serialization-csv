package app.softwork.serialization.flf

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import java.io.File
import java.io.FileOutputStream
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.util.Spliterators.AbstractSpliterator
import java.util.function.Consumer
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.streams.asSequence

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
                    }, { length ->
                        val buffer = CharBuffer.allocate(length)
                        val got = read(buffer)
                        if (got != length) {
                            throw NoMoreDataException()
                        }
                        buffer.position(0)
                        buffer
                    }, format.serializersModule, -1)
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
    return Iterable { asSequence().decode(deserializer, format).iterator() }
}

@ExperimentalSerializationApi
@JvmOverloads
public fun <T> Stream<T>.encode(
    serializer: SerializationStrategy<T>,
    format: FixedLengthFormat = FixedLengthFormat
): Iterable<String> {
    return Iterable { asSequence().encode(serializer, format).iterator() }
}

@ExperimentalSerializationApi
@JvmOverloads
public fun <T> Stream<String>.decodeStream(
    deserializer: DeserializationStrategy<T>,
    format: FixedLengthFormat = FixedLengthFormat
): Stream<T> {
    deserializer.descriptor.checkForMaps()
    val parallel = isParallel
    val split = spliterator()
    return StreamSupport.stream(
        object : AbstractSpliterator<T>(
            split.estimateSize(),
            split.characteristics().and(NONNULL)
        ) {
            var currentRow: String? = null
            val decoder = FixedLengthDecoder(
                {},
                { currentRow!! },
                format.serializersModule,
                size = exactSizeIfKnown.toIntOrNull() ?: -1
            )

            override fun tryAdvance(action: Consumer<in T>): Boolean {
                return split.tryAdvance {
                    currentRow = it
                    val t = deserializer.deserialize(decoder)
                    action.accept(t)
                }
            }

            fun Long.toIntOrNull(): Int? {
                return if (this < Int.MIN_VALUE || this > Int.MAX_VALUE) {
                    null
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
