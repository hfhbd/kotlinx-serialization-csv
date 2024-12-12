package app.softwork.serialization.csv

import kotlinx.datetime.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlin.test.*

@ExperimentalSerializationApi
class CsvEncoderTest {

    @Test
    fun normal() {
        val csv = CSVFormat.encodeToString(Foo.serializer(), Foo(bar = 42))
        assertEquals(
            expected = """
                bar
                42
            """.trimIndent(),
            actual = csv
        )
    }

    @Test
    fun named() {
        val csv = CSVFormat.encodeToString(FooNamed.serializer(), FooNamed(bar = 42))
        assertEquals(
            expected = """
                foo
                42
            """.trimIndent(),
            actual = csv
        )
    }

    @Test
    fun nullableSecond() {
        val csv = CSVFormat.encodeToString(FooNull.serializer(), FooNull(bar = 42, baz = null))
        assertEquals(
            expected = """
                bar,baz
                42,
            """.trimIndent(),
            actual = csv
        )
    }

    @Test
    fun nullableFirst() {
        val csv = CSVFormat.encodeToString(FooNullFirst.serializer(), FooNullFirst(baz = null, bar = 42))
        assertEquals(
            expected = """
                baz,bar
                ,42
            """.trimIndent(),
            actual = csv
        )
    }

    @Test
    fun nested() {
        val csv = CSVFormat.encodeToString(
            serializer = FooNested.serializer(),
            value = FooNested(
                baz = 42,
                child = FooNullFirst(baz = null, bar = 42),
                foo = 1
            )
        )
        assertEquals(
            expected = """
                baz,baz,bar,foo
                42,,42,1
            """.trimIndent(),
            actual = csv
        )
    }

    @Test
    fun nestedList() {
        assertFailsWith<IllegalStateException> {
            val csv = CSVFormat.encodeToString(
                serializer = FooList.serializer(),
                value = FooList(
                    baz = 42,
                    child = listOf(
                        FooNullFirst(baz = null, bar = 1),
                        FooNullFirst(baz = null, bar = 2)
                    )
                )
            )
            assertEquals(
                expected = """
                    baz,baz,bar,baz,bar
                    42,,1,,2
                """.trimIndent(),
                actual = csv
            )
        }
    }

    @Test
    fun list() {
        val csv = CSVFormat.encodeToString(
            serializer = ListSerializer(elementSerializer = FooNested.serializer()),
            value = List(size = 3) {
                FooNested(
                    baz = 42,
                    child = FooNullFirst(baz = null, bar = it),
                    foo = it * 10
                )
            }
        )

        assertEquals(
            expected = """
                baz,baz,bar,foo
                42,,0,0
                42,,1,10
                42,,2,20
            """.trimIndent(),
            actual = csv
        )
    }

    @Test
    fun emptyListTest() {
        val csv = CSVFormat.encodeToString(ListSerializer(FooNested.serializer()), emptyList())

        assertEquals(
            expected = """
                baz,baz,bar,foo
            """.trimIndent(),
            actual = csv
        )
    }

    @Test
    fun enumTest() {
        val csv = CSVFormat.encodeToString(
            serializer = FooEnum.serializer(),
            value = FooEnum(
                baz = null,
                foo = FooEnum.A.One
            )
        )

        assertEquals(
            expected = """
                baz,foo
                ,One
            """.trimIndent(),
            actual = csv
        )
    }

    @Test
    fun inlineTest() {
        val csv = CSVFormat.encodeToString(FooInline.serializer(), FooInline(42.42))

        assertEquals(
            expected = """
                foo
                42.42
            """.trimIndent(),
            actual = csv
        )
    }

    @Test
    fun complexTest() {
        val csv = CSVFormat.encodeToString(
            serializer = ListSerializer(FooComplex.serializer()),
            value = List(size = 3) {
                FooComplex(
                    bar = if (it == 1) "Something" else null,
                    inline = FooInline(42.42),
                    enum = FooEnum.A.Three,
                    instant = Instant.fromEpochSeconds(it.toLong())
                )
            }
        )

        assertEquals(
            expected = """
                bar,foo,enum,instant
                ,42.42,Three,1970-01-01T00:00:00Z
                Something,42.42,Three,1970-01-01T00:00:01Z
                ,42.42,Three,1970-01-01T00:00:02Z
            """.trimIndent(),
            actual = csv
        )
    }

    @Test
    fun custom() {
        val csv = CSVFormat(separator = ";", lineSeparator = "\r\n").encodeToString(
            FooNull.serializer(),
            FooNull(bar = 42, baz = null)
        )

        assertEquals(
            expected = "bar;baz\r\n42;",
            actual = csv
        )
    }

    @Test
    fun numberFormatTest() {
        val csv = CSVFormat(
            separator = ";",
            lineSeparator = "\r\n",
            numberFormat = CSVFormat.NumberFormat.Comma
        ).encodeToString(
            serializer = ListSerializer(FooComplex.serializer()),
            value = List(size = 3) {
                FooComplex(
                    bar = if (it == 1) "Something" else null,
                    inline = FooInline(42.42),
                    enum = FooEnum.A.Three,
                    instant = Instant.fromEpochSeconds(it.toLong())
                )
            }
        )

        assertEquals(
            expected = "bar;foo;enum;instant\r\n;42,42;Three;1970-01-01T00:00:00Z\r\nSomething;42,42;Three;1970-01-01T00:00:01Z\r\n;42,42;Three;1970-01-01T00:00:02Z",
            actual = csv
        )
    }

    @Test
    fun checkForPolymorphicClasses() {
        assertFailsWith<IllegalArgumentException> {
            CSVFormat.encodeToString(ListSerializer(Sealed.serializer()), emptyList())
        }
    }

    @Test
    fun customList() {
        val csv = CSVFormat(
            separator = ";",
            lineSeparator = "\r\n",
            includeHeader = false,
        ).encodeToString(
            ListSerializer(Sealed.serializer()),
            listOf(
                Sealed.Foo("Hello ;from\r\nWorld"),
                Sealed.Bar(42)
            )
        )

        assertEquals(
            expected = "foo;\"Hello ;from\r\nWorld\"\r\nbar;42",
            actual = csv
        )
    }

    @Test
    fun alwaysQuote() {
        val csv = CSVFormat(
            alwaysEmitQuotes = true,
            includeHeader = false,
        ).encodeToString(
            ListSerializer(Sealed.serializer()),
            listOf(Sealed.Foo("Hello from\nWorld"), Sealed.Bar(42))
        )

        assertEquals(
            expected = "\"foo\",\"Hello from\nWorld\"\n\"bar\",\"42\"",
            actual = csv
        )
    }
}
