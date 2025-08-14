package app.softwork.serialization.flf

import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalSerializationApi
class JvmExtensionsTest {
    @Test
    fun sequences() {
        val file = File.createTempFile("foo", "bar")
        val data = sequenceOf("1", "2", "3")
        data.writeLines(file)
        data.appendLines(file)

        assertEquals(listOf("1", "2", "3", "1", "2", "3"), file.readLines())
    }

    @Test
    fun appendWrite() {
        assertEquals(
            "11\n",
            buildString {
                append(
                    Small.serializer(),
                    Small("1")
                )
                appendLine(
                    Small.serializer(),
                    Small("1")
                )
            }
        )
    }

    @Test
    fun decode() {
        assertEquals(
            listOf(
                Small("1"),
                Small("2"),
                Small("3"),
            ),
            "123".reader()
                .decode(
                    deserializer = Small.serializer(),
                    format = FixedLengthFormat.Default(lineSeparator = "")
                ).toList()
        )

        assertEquals(
            listOf(
                Small("4"),
                Small("5"),
                Small("6"),
            ),
            "4\n5\n6\n".reader()
                .decode(
                    deserializer = Small.serializer(),
                ).toList()
        )
    }

    @Test
    fun stream() {
        val stream = Stream.of("a", "b")
        assertEquals(listOf(Small("a"), Small("b")), stream.decode(Small.serializer()).toList())

        val parallelStream = Stream.of("a", "b").parallel()
        assertEquals(
            listOf("a", "b"),
            parallelStream.decodeStream(Small.serializer()).encodeStream(Small.serializer()).collect(
                Collectors.toList()
            )
        )
    }
}
