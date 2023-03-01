package app.softwork.serialization.flf

import app.softwork.serialization.flf.Ebcdic.Format.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.test.*

@ExperimentalSerializationApi
class EbcdicTest {

    @Serializable
    data class Formatting(
        @Ebcdic(Zoned)
        @FixedLength(4)
        val positive: Int,

        @Ebcdic(Zoned)
        @FixedLength(4)
        val negative: Int,

        @FixedLength(4)
        val unsigned: UInt,

        @FixedLength(4)
        val normal: Int
    )

    @Test
    fun encode() {
        val value = Formatting(
            positive = 42,
            negative = -42,
            unsigned = 42u,
            normal = 42
        )

        assertEquals(
            """
            |{"positive":42,"negative":-42,"unsigned":42,"normal":42}
        """.trimMargin(),
            Json.encodeToString(
                serializer = Formatting.serializer(),
                value = value
            )
        )

        assertEquals(
            expected = """
            |004B004K00420042
            """.trimMargin(),
            actual = FixedLengthFormat.encodeToString(
                serializer = Formatting.serializer(),
                value = value
            )
        )
    }

    @Test
    fun decode() {
        val value = Formatting(
            positive = 42,
            negative = -42,
            unsigned = 42u,
            normal = 42
        )

        assertEquals(
            value,
            Json.decodeFromString(
                deserializer = Formatting.serializer(),
                """
            |{"positive":42,"negative":-42,"unsigned":42,"normal":42}
        """.trimMargin()
            )
        )

        assertEquals(
            expected = value,
            actual = FixedLengthFormat.decodeFromString(
                deserializer = Formatting.serializer(),
                """
            |004B004K00420042
            """.trimMargin(),
            )
        )
    }

    @Test
    fun zonedToString() {
        assertEquals("123D", Zoned.toString(1234))
        assertEquals("123M", Zoned.toString(-1234))

        assertEquals("1N", Zoned.toString(-15))
        assertEquals("150{", Zoned.toString(1500))

        assertEquals("1M", Zoned.toString(-14))
        assertEquals("140{", Zoned.toString(1400))

        assertEquals("1L", Zoned.toString(-13))
        assertEquals("130{", Zoned.toString(1300))

        assertEquals("1K", Zoned.toString(-12))
        assertEquals("120{", Zoned.toString(1200))

        assertEquals("1J", Zoned.toString(-11))
        assertEquals("110{", Zoned.toString(1100))

        assertEquals("1}", Zoned.toString(-10))
        assertEquals("100{", Zoned.toString(1000))

        assertEquals("R", Zoned.toString(-9))
        assertEquals("90{", Zoned.toString(900))

        assertEquals("Q", Zoned.toString(-8))
        assertEquals("80{", Zoned.toString(800))

        assertEquals("P", Zoned.toString(-7))
        assertEquals("70{", Zoned.toString(700))

        assertEquals("O", Zoned.toString(-6))
        assertEquals("60{", Zoned.toString(600))

        assertEquals("N", Zoned.toString(-5))
        assertEquals("50{", Zoned.toString(500))

        assertEquals("M", Zoned.toString(-4))
        assertEquals("40{", Zoned.toString(400))

        assertEquals("L", Zoned.toString(-3))
        assertEquals("30{", Zoned.toString(300))

        assertEquals("K", Zoned.toString(-2))
        assertEquals("20{", Zoned.toString(200))

        assertEquals("J", Zoned.toString(-1))
        assertEquals("10{", Zoned.toString(100))

        assertEquals("{", Zoned.toString(0))
        assertEquals("{", Zoned.toString(0L))
    }

    @Test
    fun zonedFromString() {
        assertEquals(1234, Zoned.toInt("123D"))
        assertEquals(-1234, Zoned.toInt("123M"))

        assertEquals(-15, Zoned.toInt("1N"))
        assertEquals(1500, Zoned.toInt("150{"))

        assertEquals(-14, Zoned.toInt("1M"))
        assertEquals(1400, Zoned.toInt("140{"))

        assertEquals(-13, Zoned.toInt("1L"))
        assertEquals(1300, Zoned.toInt("130{"))

        assertEquals(-12, Zoned.toInt("1K"))
        assertEquals(1200, Zoned.toInt("120{"))

        assertEquals(-11, Zoned.toInt("1J"))
        assertEquals(1100, Zoned.toInt("110{"))

        assertEquals(-10, Zoned.toInt("1}"))
        assertEquals(1000, Zoned.toInt("100{"))

        assertEquals(-9, Zoned.toInt("R"))
        assertEquals(900, Zoned.toInt("90{"))

        assertEquals(-8, Zoned.toInt("Q"))
        assertEquals(800, Zoned.toInt("80{"))

        assertEquals(-7, Zoned.toInt("P"))
        assertEquals(700, Zoned.toInt("70{"))

        assertEquals(-6, Zoned.toInt("O"))
        assertEquals(600, Zoned.toInt("60{"))

        assertEquals(-5, Zoned.toInt("N"))
        assertEquals(500, Zoned.toInt("50{"))

        assertEquals(-4, Zoned.toInt("M"))
        assertEquals(400, Zoned.toInt("40{"))

        assertEquals(-3, Zoned.toInt("L"))
        assertEquals(300, Zoned.toInt("30{"))

        assertEquals(-2, Zoned.toInt("K"))
        assertEquals(200, Zoned.toInt("20{"))

        assertEquals(-1, Zoned.toInt("J"))
        assertEquals(100, Zoned.toInt("10{"))

        assertEquals(0, Zoned.toInt("{"))
        assertEquals(0, Zoned.toInt("}"))
        assertEquals(0L, Zoned.toLong("{"))
        assertEquals(0L, Zoned.toLong("}"))
    }
}
