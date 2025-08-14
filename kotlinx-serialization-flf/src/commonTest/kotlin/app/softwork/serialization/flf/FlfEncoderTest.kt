package app.softwork.serialization.flf

import app.softwork.serialization.flf.Seal.A
import app.softwork.serialization.flf.Seal.B
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

@ExperimentalSerializationApi
class FlfEncoderTest {

    @Test
    fun normal() {
        val flf = FixedLengthFormat.encodeToString(
            serializer = Sample.serializer(),
            value = Sample.simple
        )
        assertEquals(
            expected = """
                ShortLong      004242.3    1970-01-01T00:00:00ZTwo  001foo4.2true000f41.118false0001000104.2-001 
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
                    inlineFloat = Sample.FooFloat(1.1),
                    inlineByte = Sample.FooByte(1.toByte()),
                    innerClass = Sample.Inner(8),
                    boolean = false,
                    byte = 1.toByte(),
                    short = 1.toShort(),
                    float = 4.2,
                    long = -1L,
                    char = ' '
                )
            }
        )
        assertEquals(
            expected = """
                ShortLong      000042.3    1970-01-01T00:00:00ZTwo  000foo4.2true000f41.118false0001000104.2-001 
                ShortLong      000142.3    1970-01-01T00:00:00ZTwo  001foo4.2true000f41.118false0001000104.2-001 
                ShortLong      000242.3    1970-01-01T00:00:00ZTwo  002foo4.2true000f41.118false0001000104.2-001 
            """.trimIndent(),
            actual = flf
        )
    }

    @Test
    fun sequence() {
        val flf = generateSequence(0) { it + 1 }.map {
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
                inlineFloat = Sample.FooFloat(1.1),
                inlineByte = Sample.FooByte(1.toByte()),
                innerClass = Sample.Inner(8),
                boolean = false,
                byte = 1.toByte(),
                short = 1.toShort(),
                float = 4.2,
                long = -1L,
                char = ' '
            )
        }.take(3).constrainOnce()

        assertEquals(
            expected = listOf(
                "ShortLong      000042.3    1970-01-01T00:00:00ZTwo  000foo4.2true000f41.118false0001000104.2-001 ",
                "ShortLong      000142.3    1970-01-01T00:00:00ZTwo  001foo4.2true000f41.118false0001000104.2-001 ",
                "ShortLong      000242.3    1970-01-01T00:00:00ZTwo  002foo4.2true000f41.118false0001000104.2-001 "
            ),
            actual = flf.encode(Sample.serializer()).toList()
        )

        val constrainOnce = assertFailsWith<IllegalStateException> {
            flf.encode(Sample.serializer()).count()
        }
        assertEquals("This sequence can be consumed only once.", constrainOnce.message!!)
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
        val flf = FixedLengthFormat.encodeToString(
            InnerList.serializer(),
            InnerList(
                2,
                "foo",
                listOf(
                    A(42, 1),
                    B("foo", 2)
                )
            )
        )
        assertEquals(
            expected = """
            2fooA420001Bfoo       0002
            """.trimIndent(),
            actual = flf
        )
    }

    @Test
    fun sealed() {
        val flf = FixedLengthFormat.encodeToString(
            serializer = ListSerializer(Seal.serializer()),
            value = listOf(
                A(42, 1),
                B("foo", 2)
            )
        )
        assertEquals(
            expected = """
                A420001
                Bfoo       0002
            """.trimIndent(),
            actual = flf
        )
    }

    @Test
    fun sealedProperty() {
        val flf = FixedLengthFormat.encodeToString(
            serializer = ListSerializer(SealedWithProperty.serializer()),
            value = listOf(
                SealedWithProperty(A.serializer().descriptor.serialName, 42, A(42, 1)),
                SealedWithProperty(B.serializer().descriptor.serialName, 42, B("foo", 2))
            )
        )
        assertEquals(
            expected = """
                A 42420001
                B 42foo       0002
            """.trimIndent(),
            actual = flf
        )
    }
}
