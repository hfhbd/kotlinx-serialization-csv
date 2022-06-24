package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.test.*

@ExperimentalSerializationApi
class JsonTest {
    @Test
    fun interop() {
        val sampleFlf = """
                ShortLong      42  42.3    1970-01-01T00:00:00ZTwo  1  foo4.2true0  f41.118false1   1   4.2 -1   
            """.trimIndent()
        val decode = FixedLengthFormat.decodeFromString(
            deserializer = Sample.serializer(),
            string = sampleFlf
        )
        assertEquals(
            Sample.sample,
            decode
        )
        val jsonString = Json { prettyPrint = true }.encodeToString(Sample.serializer(), decode)
        //language=JSON
        assertEquals(
            """
            {
                "shortString": "Short",
                "longString": "Long",
                "int": 42,
                "double": 42.3,
                "nil": null,
                "date": "1970-01-01T00:00:00Z",
                "enum": "Two",
                "inline": 1,
                "inlineS": "foo",
                "inlineD": 4.2,
                "inlineB": true,
                "inlineL": 0,
                "inlineChar": "f",
                "inlineShort": 4,
                "inlineFloat": 1.1,
                "inlineByte": 1,
                "innerClass": {
                    "s": 8
                },
                "boolean": false,
                "byte": 1,
                "short": 1,
                "float": 4.2,
                "long": -1,
                "char": " "
            }
            """.trimIndent(),
            jsonString
        )
        val sampleJson = Json.decodeFromString(Sample.serializer(), jsonString)
        assertEquals(
            Sample.sample,
            sampleJson
        )
        val flfString = FixedLengthFormat.encodeToString(
            serializer = Sample.serializer(),
            value = sampleJson
        )
        assertEquals(
            sampleFlf,
            flfString
        )
    }
}
