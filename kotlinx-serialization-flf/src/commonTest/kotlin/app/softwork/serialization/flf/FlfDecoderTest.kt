package app.softwork.serialization.flf

import kotlinx.datetime.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlin.test.*

@ExperimentalSerializationApi
class FlfDecoderTest {

    @Test
    fun normal() {
        val flf = """
            ShortLong      42  42.3    1970-01-01T00:00:00ZTwo  1  foo4.0true0  f41.01false1   1   4.0 -1   
        """.trimIndent()

        assertEquals(
            expected = Sample(
                shortString = "Short",
                longString = "Long",
                int = 42,
                double = 42.3,
                nil = null,
                date = Instant.fromEpochSeconds(0L),
                enum = Sample.Testing.Two,
                inline = Sample.Foo(1),
                inlineS = Sample.FooS("foo"),
                inlineD = Sample.FooD(4.0),
                inlineB = Sample.FooB(true),
                inlineL = Sample.FooL(0L),
                inlineChar = Sample.FooChar('f'),
                inlineShort = Sample.FooShort(4.toShort()),
                inlineFloat = Sample.FooFloat(1f),
                inlineByte = Sample.FooByte(1.toByte()),
                boolean = false,
                byte = 1.toByte(),
                short = 1.toShort(),
                float = 4.0f,
                long = -1L,
                char = ' '
            ),
            actual = FixedLengthFormat.decodeFromString(Sample.serializer(), flf)
        )
    }

    @Test
    fun list() {
        val flf = """
            ShortLong      0   42.3    1970-01-01T00:00:00ZTwo  0  foo4.0true0  f41.01false1   1   4.0 -1   
            ShortLong      1   42.3    1970-01-01T00:00:00ZTwo  1  foo4.0true0  f41.01false1   1   4.0 -1   
            ShortLong      2   42.3    1970-01-01T00:00:00ZTwo  2  foo4.0true0  f41.01false1   1   4.0 -1   
        """.trimIndent()

        assertEquals(
            expected = List(3) {
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
                    inlineD = Sample.FooD(4.0),
                    inlineB = Sample.FooB(true),
                    inlineL = Sample.FooL(0L),
                    inlineChar = Sample.FooChar('f'),
                    inlineShort = Sample.FooShort(4.toShort()),
                    inlineFloat = Sample.FooFloat(1f),
                    inlineByte = Sample.FooByte(1.toByte()),
                    boolean = false,
                    byte = 1.toByte(),
                    short = 1.toShort(),
                    float = 4.0f,
                    long = -1L,
                    char = ' '
                )
            },
            actual = FixedLengthFormat.decodeFromString(ListSerializer(Sample.serializer()), flf)
        )
    }

    @Test
    fun primitives() {
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(Int.serializer(), "")
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(Boolean.serializer(), "")
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(String.serializer(), "")
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(Double.serializer(), "")
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(Float.serializer(), "")
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(Byte.serializer(), "")
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(Char.serializer(), "")
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(Long.serializer(), "")
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(Short.serializer(), "")
        }
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(Int.serializer().nullable, "")
        }
    }

    @Test
    fun missingFixedLength() {
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(Missing.serializer(), "")
        }
    }

    @Test
    fun innerList() {
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(InnerList.serializer(), "")
        }
    }

    @Test
    fun enumTest() {
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(Sample.Testing.serializer(), "One")
        }
    }

    @Test
    fun inlineTest() {
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(Sample.FooShort.serializer(), "1")
        }
    }
}
