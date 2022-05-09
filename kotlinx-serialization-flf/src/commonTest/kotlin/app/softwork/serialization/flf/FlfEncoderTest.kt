package app.softwork.serialization.flf

import kotlinx.datetime.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlin.test.*

@ExperimentalSerializationApi
class FlfEncoderTest {

    @Test
    fun normal() {
        val flf = FixedLengthFormat.encodeToString(
            serializer = Sample.serializer(),
            value = Sample(
                shortString = "Short",
                longString = "Long",
                int = 42,
                double = 42.3,
                nil = null,
                date = Instant.fromEpochSeconds(0L),
                enum = Sample.Testing.Two,
                inline = Sample.Foo(1),
                inlineS = Sample.FooS("foo"),
                inlineD = Sample.FooD(4.2),
                inlineB = Sample.FooB(true),
                inlineL = Sample.FooL(0L),
                inlineChar = Sample.FooChar('f'),
                inlineShort = Sample.FooShort(4.toShort()),
                inlineFloat = Sample.FooFloat(1.1f),
                inlineByte = Sample.FooByte(1.toByte()),
                innerClass = Sample.Inner(8),
                boolean = false,
                byte = 1.toByte(),
                short = 1.toShort(),
                float = 4.2f,
                long = -1L,
                char = ' '
            )
        )
        assertEquals(
            expected = """
                ShortLong      42  42.3    1970-01-01T00:00:00ZTwo  1  foo4.2true0  f41.118false1   1   4.2 -1   
            """.trimIndent(),
            actual = flf
        )
    }

    @Test
    fun list() {
        val flf = FixedLengthFormat.encodeToString(
            serializer = ListSerializer(Sample.serializer()),
            value = List(3) {
                Sample(
                    shortString = "Short",
                    longString = "Long",
                    int = it,
                    double = 42.3,
                    nil = null,
                    date = Instant.fromEpochSeconds(0L),
                    enum = Sample.Testing.Two,
                    inline = Sample.Foo(it),
                    inlineS = Sample.FooS("foo"),
                    inlineD = Sample.FooD(4.2),
                    inlineB = Sample.FooB(true),
                    inlineL = Sample.FooL(0L),
                    inlineChar = Sample.FooChar('f'),
                    inlineShort = Sample.FooShort(4.toShort()),
                    inlineFloat = Sample.FooFloat(1.1f),
                    inlineByte = Sample.FooByte(1.toByte()),
                    innerClass = Sample.Inner(8),
                    boolean = false,
                    byte = 1.toByte(),
                    short = 1.toShort(),
                    float = 4.2f,
                    long = -1L,
                    char = ' '
                )
            }
        )
        assertEquals(
            expected = """
                ShortLong      0   42.3    1970-01-01T00:00:00ZTwo  0  foo4.2true0  f41.118false1   1   4.2 -1   
                ShortLong      1   42.3    1970-01-01T00:00:00ZTwo  1  foo4.2true0  f41.118false1   1   4.2 -1   
                ShortLong      2   42.3    1970-01-01T00:00:00ZTwo  2  foo4.2true0  f41.118false1   1   4.2 -1   
            """.trimIndent(),
            actual = flf
        )
    }

    @Test
    fun primitives() {
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.encodeToString(1)
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.encodeToString(true)
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.encodeToString("")
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.encodeToString(1.0)
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.encodeToString(1.0f)
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.encodeToString(1.toByte())
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.encodeToString(1.toChar())
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.encodeToString(1L)
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.encodeToString(1.toShort())
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.encodeToString(null as Int?)
        }
    }

    @Test
    fun tooSmall() {
        assertFailsWith<IllegalArgumentException> {
            FixedLengthFormat.encodeToString(Small("Too long"))
        }
    }

    @Test
    fun tooSmallNull() {
        assertEquals(" ", FixedLengthFormat.encodeToString(Small(null)))
    }

    @Test
    fun missingFixedLength() {
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.encodeToString(Missing.serializer(), Missing(42))
        }
    }

    @Test
    fun innerList() {
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.encodeToString(InnerList.serializer(), InnerList(emptyList()))
        }
    }
}
