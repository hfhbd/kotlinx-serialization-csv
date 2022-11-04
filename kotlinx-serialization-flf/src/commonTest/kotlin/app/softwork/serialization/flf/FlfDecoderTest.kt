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
            ShortLong      42  42.3    1970-01-01T00:00:00ZTwo  1  foo4.2true0  f41.118false1   1   4.2 -1   
        """.trimIndent()

        assertEquals(
            expected = Sample.simple,
            actual = FixedLengthFormat.decodeFromString(Sample.serializer(), flf)
        )
    }

    @Test
    fun list() {
        val flf = """
            ShortLong      0   42.3    1970-01-01T00:00:00ZTwo  0  foo4.0true0  f41.018false1   1   4.0 -1   
            ShortLong      1   42.3    1970-01-01T00:00:00ZTwo  1  foo4.0true0  f41.018false1   1   4.0 -1   
            ShortLong      2   42.3    1970-01-01T00:00:00ZTwo  2  foo4.0true0  f41.018false1   1   4.0 -1   
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
                    innerClass = Sample.Inner(8),
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
    fun sequence() {
        val flf = sequence {
            yield("ShortLong      0   42.3    1970-01-01T00:00:00ZTwo  0  foo4.0true0  f41.018false1   1   4.0 -1   ")
            yield("ShortLong      1   42.3    1970-01-01T00:00:00ZTwo  1  foo4.0true0  f41.018false1   1   4.0 -1   ")
            yield("ShortLong      2   42.3    1970-01-01T00:00:00ZTwo  2  foo4.0true0  f41.018false1   1   4.0 -1   ")
        }
        val actual = flf.decode(Sample.serializer())

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
                    innerClass = Sample.Inner(8),
                    boolean = false,
                    byte = 1.toByte(),
                    short = 1.toShort(),
                    float = 4.0f,
                    long = -1L,
                    char = ' '
                )
            },
            actual = actual.toList()
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

    @Test
    fun sealed() {
        val flf = """
                A421   
                Bfoo       2
        """.trimIndent()

        assertEquals(
            expected = listOf(
                Seal.A(42, 1),
                Seal.B("foo", 2)
            ),
            actual = FixedLengthFormat.decodeFromString(ListSerializer(Seal.serializer()), flf)
        )
    }

    @Test
    fun innerList() {
        val flf = """
            2fooA421   Bfoo       2   
        """.trimIndent()
        assertEquals(
            expected = InnerList(
                2,
                "foo",
                listOf(
                    Seal.A(42, 1),
                    Seal.B("foo", 2)
                )
            ),
            actual = FixedLengthFormat.decodeFromString(InnerList.serializer(), flf)
        )
    }

    @Test
    fun innerListFailing() {
        val flf = """
            fooA421   Bfoo       2   2
        """.trimIndent()
        assertFailsWith<IllegalStateException> {
            FixedLengthFormat.decodeFromString(InnerListFailing.serializer(), flf)
        }
    }

    @Test
    fun sealedProperty() {
        val flf = FixedLengthFormat.decodeFromString(
            deserializer = ListSerializer(SealedWithProperty.serializer()),
            string = """
                A 42421   
                B 42foo       2   
            """.trimIndent()
        )
        assertEquals(
            expected = listOf(
                SealedWithProperty(Seal.A.serializer().descriptor.serialName, 42, Seal.A(42, 1)),
                SealedWithProperty(Seal.B.serializer().descriptor.serialName, 42, Seal.B("foo", 2))
            ),
            actual = flf
        )
    }
}
