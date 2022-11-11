package app.softwork.serialization.flf

import kotlinx.serialization.*
import java.io.*
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
}
