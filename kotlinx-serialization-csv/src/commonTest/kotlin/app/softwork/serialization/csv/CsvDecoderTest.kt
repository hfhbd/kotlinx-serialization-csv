package app.softwork.serialization.csv

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
            42,,42,1
        """.trimIndent()

        assertEquals(
            expected = FooNested(
                baz = 42,
                child = FooNullFirst(baz = null, bar = 42),
                foo = 1
            ),
            actual = CSVFormat.decodeFromString(FooNested.serializer(), csv)
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
                ListSerializer(FooNested.serializer()), csv
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
    fun inlineTest() {
        val csv = """
            foo
            42
        """.trimIndent()

        assertEquals(
            expected = FooInline(42),
            actual = CSVFormat.decodeFromString(FooInline.serializer(), csv)
        )
    }

    @Test
    fun complexTest() {
        val csv = """
            bar,foo,enum
            ,42,Three
            Something,42,Three
            ,42,Three
        """.trimIndent()

        assertEquals(
            expected = List(3) {
                FooComplex(
                    bar = if (it == 1) "Something" else null,
                    inline = FooInline(42),
                    enum = FooEnum.A.Three
                )
            },
            actual = CSVFormat.decodeFromString(ListSerializer(FooComplex.serializer()), csv)
        )
    }
}
