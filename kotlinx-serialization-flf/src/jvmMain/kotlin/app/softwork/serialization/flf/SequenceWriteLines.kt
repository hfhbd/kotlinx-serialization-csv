package app.softwork.serialization.flf

import kotlinx.serialization.*
import java.io.*
import java.nio.charset.*
import java.util.stream.*
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
public fun <T> decodeStream(
    lines: Stream<String>,
    serializer: DeserializationStrategy<T>,
    format: FixedLengthFormat = FixedLengthFormat
): Iterable<T> {
    return Iterable { lines.asSequence().decode(serializer, format).iterator() }
}

@ExperimentalSerializationApi
@JvmOverloads
public fun <T> Appendable.append(
    serializer: SerializationStrategy<T>,
    value: T,
    format: FixedLengthFormat = FixedLengthFormat
) {
    appendLine(format.encodeToString(serializer, value))
}
