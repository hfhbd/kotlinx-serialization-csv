package app.softwork.serialization.csv

import app.softwork.serialization.csv.CSVNode.Element
import app.softwork.serialization.csv.CSVNode.NewLine
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlin.test.*
import kotlin.time.*

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
            listOf(
                Element("bar"),
                Element("baz"),
                NewLine,

                Element("42"),
            ),
            csv.parse().asSequence().toList()
        )

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
            listOf(
                Element("baz"),
                Element("baz"),
                Element("bar"),
                Element("foo"),
                NewLine,
                Element("42"),
                Element(""),
                Element("43"),
                Element("1"),
            ),
            csv.parse(',', "\n").asSequence().toList()
        )

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
    fun unordered() {
        val csv = """
            foo,bar,value
            1,42,value
        """.trimIndent()

        assertEquals(
            listOf(
                Element("foo"),
                Element("bar"),
                Element("value"),
                NewLine,
                Element("1"),
                Element("42"),
                Element("value"),
            ),
            csv.parse(',', "\n").asSequence().toList()
        )

        assertEquals(
            expected = listOf(
                FooString(
                    bar = 42,
                    value = "value",
                    foo = 1
                )
            ),
            actual = CSVFormat.decodeFromString(ListSerializer(FooString.serializer()), csv)
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

        assertEquals(
            expected = listOf(
                Element("baz"),
                Element("foo"),
                NewLine,
                Element(""),
                Element("One"),
            ),
            actual = csv.parse(separator = ',', lineSeparator = "\n").asSequence().toList(),
        )
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
                separator = ';'
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
        //language=csv
        val csv = """
            bar,foo,enum,instant,more
            ,42,One,1970-01-01T00:00:00Z,more
            Something,42,Two,1970-01-01T00:00:01Z,more
            ,42,Three,1970-01-01T00:00:02Z,more
        """.trimIndent()

        assertEquals(
            listOf(
                Element(value = "bar"),
                Element(value = "foo"),
                Element(value = "enum"),
                Element(value = "instant"),
                Element(value = "more"),
                NewLine,

                Element(value = ""),
                Element(value = "42"),
                Element(value = "One"),
                Element(value = "1970-01-01T00:00:00Z"),
                Element(value = "more"),
                NewLine,

                Element(value = "Something"),
                Element(value = "42"),
                Element(value = "Two"),
                Element(value = "1970-01-01T00:00:01Z"),
                Element(value = "more"),
                NewLine,

                Element(value = ""),
                Element(value = "42"),
                Element(value = "Three"),
                Element(value = "1970-01-01T00:00:02Z"),
                Element(value = "more")
            ),
            csv.parse().asSequence().toList()
        )

        assertEquals(
            expected = listOf(
                FooComplex(
                    bar = null,
                    inline = FooInline(42.0),
                    enum = FooEnum.A.One,
                    instant = Instant.fromEpochSeconds(0)
                ),
                FooComplex(
                    bar = "Something",
                    inline = FooInline(42.0),
                    enum = FooEnum.A.Two,
                    instant = Instant.fromEpochSeconds(1)
                ),
                FooComplex(
                    bar = null,
                    inline = FooInline(42.0),
                    enum = FooEnum.A.Three,
                    instant = Instant.fromEpochSeconds(2)
                )
            ),
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
        assertEquals("Missing value at the end of line 2", exception.message)
    }

    @Test
    fun missingMultipleNullableValuesTest() {
        val csv = """
            bar,baz1,baz2,baz3
            40,,,
            41,,
            42,
            43
        """.trimIndent()

        assertEquals(
            listOf(
                Element("bar"),
                Element("baz1"),
                Element("baz2"),
                Element("baz3"),
                NewLine,
                Element("40"),
                Element(""),
                Element(""),
                Element(""),
                NewLine,
                Element("41"),
                Element(""),
                Element(""),
                NewLine,
                Element("42"),
                Element(""),
                NewLine,
                Element("43")
            ),
            csv.parse().asSequence().toList()
        )

        assertEquals(
            expected = listOf(
                FooMultipleNull(40, null, null, null),
                FooMultipleNull(41, null, null, null),
                FooMultipleNull(42, null, null, null),
                FooMultipleNull(43, null, null, null),
            ),
            actual = CSVFormat.decodeFromString(ListSerializer(FooMultipleNull.serializer()), csv)
        )
    }

    @Test
    fun custom() {
        val csv = "bar;baz\r\n42;\r\n"

        assertEquals(
            listOf(
                Element("bar"),
                Element("baz"),
                NewLine,
                Element("42"),
                Element(""),
                NewLine,
            ),
            csv.parse(';', "\r\n").asSequence().toList()
        )

        assertEquals(
            expected = listOf(FooNull(bar = 42, baz = null)),
            actual = CSVFormat {
                separator = ';'
                lineSeparator = "\r\n"
            }.decodeFromString(ListSerializer(FooNull.serializer()), csv)
        )
    }

    @Test
    fun quotesWithSealedClassWithoutHeaders() {
        // language=csv
        val csv =
            "\"bar\";\"42\"\r\n\"foo\";\"Some long\r\nvalue with\nnewline\"\r\n\"bar\";\"1\""

        assertEquals(
            listOf(
                Element(value = "bar"),
                Element(value = "42"),
                NewLine,

                Element(value = "foo"),
                Element(value = "Some long\r\nvalue with\nnewline"),
                NewLine,

                Element(value = "bar"),
                Element(value = "1")
            ),
            csv.parse(';', "\r\n").asSequence().toList()
        )

        assertEquals(
            expected = listOf(Sealed.Bar(42), Sealed.Foo("Some long\r\nvalue with\nnewline"), Sealed.Bar(1)),
            actual = CSVFormat {
                separator = ';'
                lineSeparator = "\r\n"
                includeHeader = false
            }.decodeFromString(ListSerializer(Sealed.serializer()), csv)
        )
    }

    @Test
    fun simpleQuotes() {
        // language=csv
        val csv = "\"bar\"\n\"42\"\n11"

        assertEquals(
            listOf(
                Element(value = "bar"),
                NewLine,
                Element(value = "42"),
                NewLine,
                Element(value = "11"),
            ),
            csv.parse().asSequence().toList()
        )

        assertEquals(
            expected = listOf(
                Foo(42),
                Foo(11),
            ),
            actual = CSVFormat.decodeFromString(
                ListSerializer(
                    Foo.serializer(),
                ),
                csv
            )
        )
    }

    @Test
    fun simpleQuotesWithNewLine() {
        // language=csv
        val csv = "\"bar\",value,\"foo\"\n\"42\",\"asf\nsadfh\n\",\"42\"\n11,asdf,1\n"

        assertEquals(
            listOf(
                Element(value = "bar"),
                Element(value = "value"),
                Element(value = "foo"),
                NewLine,
                Element(value = "42"),
                Element(value = "asf\nsadfh\n"),
                Element(value = "42"),
                NewLine,
                Element(value = "11"),
                Element(value = "asdf"),
                Element(value = "1"),
                NewLine,
            ),
            csv.parse().asSequence().toList()
        )

        assertEquals(
            expected = listOf(
                FooString(42, "asf\nsadfh\n", 42),
                FooString(11, "asdf", 1),
            ),
            actual = CSVFormat.decodeFromString(
                ListSerializer(
                    FooString.serializer(),
                ),
                csv
            )
        )
    }

    @Test
    fun doubleQuotes() {
        // language=csv
        val csv = "\"bar\",value,\"foo\"\n\"42\",\"ff\"\"f\na\",\"42\"\n"

        assertEquals(
            listOf(
                Element(value = "bar"),
                Element(value = "value"),
                Element(value = "foo"),
                NewLine,
                Element(value = "42"),
                Element(value = "ff\"f\na"),
                Element(value = "42"),
                NewLine,
            ),
            csv.parse().asSequence().toList()
        )

        assertEquals(
            expected = listOf(
                FooString(42, "ff\"f\na", 42),
            ),
            actual = CSVFormat.decodeFromString(
                ListSerializer(
                    FooString.serializer(),
                ),
                csv
            )
        )
    }

    @Test
    fun quotesFails() {
        // language=csv
        val csv = "\"bar\",value,\"foo\"\n\"42\",\"asf\nsadfh\n,\"42\"\n11,asdf,1\n"

        val exception = assertFailsWith<SerializationException> {
            csv.parse().asSequence().count()
        }
        assertEquals("Missing end of quotes at 49", exception.message)
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
