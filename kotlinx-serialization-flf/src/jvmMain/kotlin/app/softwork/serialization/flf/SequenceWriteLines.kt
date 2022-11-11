package app.softwork.serialization.flf

import kotlinx.serialization.*
import java.io.*
import java.nio.charset.*

public fun Sequence<String>.appendLines(file: File, charset: Charset = Charsets.UTF_8, lineSeparator: String = "\n") {
    FileOutputStream(file, true).bufferedWriter(charset).use { writer ->
        for (line in this) {
            writer.write(line)
            writer.write(lineSeparator)
        }
    }
}

public fun Sequence<String>.writeLines(file: File, charset: Charset = Charsets.UTF_8, lineSeparator: String = "\n") {
    FileOutputStream(file, false).bufferedWriter(charset).use { writer ->
        for (line in this) {
            writer.write(line)
            writer.write(lineSeparator)
        }
    }
}

@ExperimentalSerializationApi
public fun <T> Appendable.append(
    serializer: SerializationStrategy<T>,
    value: T,
    format: FixedLengthFormat = FixedLengthFormat
) {
    appendLine(format.encodeToString(serializer, value))
}
