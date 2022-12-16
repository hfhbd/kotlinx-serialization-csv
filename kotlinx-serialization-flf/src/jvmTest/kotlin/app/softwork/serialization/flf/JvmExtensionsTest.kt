package app.softwork.serialization.flf

import kotlinx.serialization.*
import java.io.*
import java.util.stream.*
import kotlin.test.*

class JvmExtensionsTest {
    @Test
    fun sequences() {
        val file = File.createTempFile("foo", "bar")
        val data = sequenceOf("1", "2", "3")
        data.writeLines(file)
        data.appendLines(file)

        assertEquals(listOf("1", "2", "3", "1", "2", "3"), file.readLines())
    }

    @ExperimentalSerializationApi
    @Test
    fun append() {
        assertEquals(
            "1\n",
            buildString {
                append(
                    Small.serializer(),
                    Small("1")
                )
            }
        )
    }

    @ExperimentalSerializationApi
    @Test
    fun stream() {
        val stream = Stream.of("a", "b")
        assertEquals(listOf(Small("a"), Small("b")), decodeStream(stream, Small.serializer()).toList())
    }
}
