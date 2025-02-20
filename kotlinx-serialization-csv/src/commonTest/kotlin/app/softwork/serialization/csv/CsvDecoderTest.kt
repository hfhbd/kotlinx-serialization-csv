package app.softwork.serialization.csv

import kotlinx.datetime.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlin.test.*

@ExperimentalSerializationApi
class CsvDecoderTest {

    @Test
    fun normal() {
        val csv = """
            bar
            42
        """.trimIndent()

        assertEquals(
            expected = Foo(bar = 42),
            actual = CSVFormat.decodeFromString(Foo.serializer(), csv)
        )
    }

    @Test
    fun ignoreEmptyLines() {
        val csv = """
            bar
            42
            
            
            
        """.trimIndent()

        assertEquals(
            expected = Foo(bar = 42),
            actual = CSVFormat.decodeFromString(Foo.serializer(), csv)
        )
    }

    @Test
    fun named() {
        val csv = """
            foo
            42
        """.trimIndent()

        assertEquals(
            expected = FooNamed(bar = 42),
            actual = CSVFormat.decodeFromString(FooNamed.serializer(), csv)
        )
    }

    @Test
    fun nullableSecond() {
        val csv = """
            bar,baz
            42,
        """.trimIndent()

        assertEquals(
            expected = FooNull(bar = 42, baz = null),
            actual = CSVFormat.decodeFromString(FooNull.serializer(), csv)
        )
    }

    @Test
    fun nullableSecondWithoutComma() {
        val csv = """
            bar,baz
            42
        """.trimIndent()

        assertEquals(
            expected = FooNull(bar = 42, baz = null),
            actual = CSVFormat.decodeFromString(FooNull.serializer(), csv)
        )
    }

    @Test
    fun nullableFirst() {
        val csv = """
            baz,bar
            ,42
        """.trimIndent()

        assertEquals(
            expected = FooNullFirst(baz = null, bar = 42),
            actual = CSVFormat.decodeFromString(FooNullFirst.serializer(), csv)
        )
    }

    @Test
    fun nested() {
        val csv = """
            baz,baz,bar,foo
            42,,43,1
        """.trimIndent()

        assertEquals(
            expected = listOf(
                FooNested(
                    baz = 42,
                    child = FooNullFirst(baz = null, bar = 43),
                    foo = 1
                )
            ),
            actual = CSVFormat.decodeFromString(ListSerializer(FooNested.serializer()), csv)
        )
    }

    @Test
    fun nestedList() {
        assertFailsWith<IllegalStateException> {
            val csv = """
                baz,baz,bar,baz,bar
                42,,1,,2
            """.trimIndent()

            assertEquals(
                expected = FooList(
                    baz = 42,
                    child = listOf(
                        FooNullFirst(baz = null, bar = 1),
                        FooNullFirst(baz = null, bar = 2)
                    )
                ),
                actual = CSVFormat.decodeFromString(FooList.serializer(), csv)
            )
        }
    }

    @Test
    fun list() {
        val csv = """
            baz,baz,bar,foo
            42,,0,0
            42,,1,10
            42,,2,20
        """.trimIndent()

        assertEquals(
            expected = List(3) {
                FooNested(
                    baz = 42,
                    child = FooNullFirst(baz = null, bar = it),
                    foo = it * 10
                )
            },
            actual = CSVFormat.decodeFromString(
                deserializer = ListSerializer(FooNested.serializer()),
                string = csv
            )
        )
    }

    @Test
    fun emptyListTest() {
        val csv = """
            baz,baz,bar,foo
        """.trimIndent()

        assertEquals(
            expected = emptyList(),
            actual = CSVFormat.decodeFromString(ListSerializer(FooNested.serializer()), csv)
        )
    }

    @Test
    fun enumTest() {
        val csv = """
            baz,foo
            ,One
        """.trimIndent()

        assertEquals(
            expected = FooEnum(baz = null, foo = FooEnum.A.One),
            actual = CSVFormat.decodeFromString(FooEnum.serializer(), csv)
        )
    }

    @Test
    fun parseEnumTest() {
        val csv = """
            baz,foo
            ,One
        """.trimIndent()
    }

    @Test
    fun inlineTest() {
        val csv = """
            foo
            42
        """.trimIndent()

        assertEquals(
            expected = FooInline(42.0),
            actual = CSVFormat.decodeFromString(FooInline.serializer(), csv)
        )
    }

    @Test
    fun numberFormatTest() {
        val csv = """
            foo
            42.42
        """.trimIndent()

        assertEquals(
            expected = FooInline(42.42),
            actual = CSVFormat.decodeFromString(FooInline.serializer(), csv)
        )

        val csv2 = """
            foo
            42,42
        """.trimIndent()

        assertEquals(
            expected = FooInline(42.42),
            actual = CSVFormat {
                numberFormat = CSVFormat.NumberFormat.Comma
                separator = ";"
            }.decodeFromString(FooInline.serializer(), csv2)
        )
    }

    @Test
    fun complexTest() {
        val csv = """
            bar,foo,enum,instant
            ,42,Three,1970-01-01T00:00:00Z
            Something,42,Three,1970-01-01T00:00:01Z
            ,42,Three,1970-01-01T00:00:02Z
        """.trimIndent()

        assertEquals(
            expected = List(3) {
                FooComplex(
                    bar = if (it == 1) "Something" else null,
                    inline = FooInline(42.0),
                    enum = FooEnum.A.Three,
                    instant = Instant.fromEpochSeconds(it.toLong())
                )
            },
            actual = CSVFormat.decodeFromString(ListSerializer(FooComplex.serializer()), csv)
        )
    }

    @Test
    fun moreAttributesTest() {
        val csv = """
            bar,foo,enum,instant,a
            ,42,Three,1970-01-01T00:00:00Z,a
            Something,42,Three,1970-01-01T00:00:01Z,a
            ,42,Three,1970-01-01T00:00:02Z,a
        """.trimIndent()

        assertEquals(
            expected = List(3) {
                FooComplex(
                    bar = if (it == 1) "Something" else null,
                    inline = FooInline(42.0),
                    enum = FooEnum.A.Three,
                    instant = Instant.fromEpochSeconds(it.toLong())
                )
            },
            actual = CSVFormat.decodeFromString(ListSerializer(FooComplex.serializer()), csv)
        )
    }

    @Test
    fun lessAttributesTest() {
        val csv = """
            bar,foo,enum
            ,42,Three
            Something,42,Three
            ,42,Three
        """.trimIndent()

        val exception = assertFailsWith<SerializationException> {
            CSVFormat.decodeFromString(ListSerializer(FooComplex.serializer()), csv)
        }
        assertEquals("Missing value at index 3 in line 2", exception.message)
    }

    @Test
    fun custom() {
        val csv = "bar;baz\r\n42;"

        assertEquals(
            expected = listOf(FooNull(bar = 42, baz = null)),
            actual = CSVFormat {
                separator = ";"
                lineSeparator = "\r\n"
            }.decodeFromString(ListSerializer(FooNull.serializer()), csv)
        )
    }

    @Test
    fun sealed() {
        val csv = "foo,some String\nbar,42"

        assertEquals(
            expected = listOf(
                Sealed.Foo("some String"),
                Sealed.Bar(42),
            ),
            actual = CSVFormat { includeHeader = false }.decodeFromString(ListSerializer(Sealed.serializer()), csv)
        )
    }
}
