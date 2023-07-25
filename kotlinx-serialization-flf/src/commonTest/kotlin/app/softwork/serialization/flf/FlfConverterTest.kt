package app.softwork.serialization.flf

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalSerializationApi
class FlfConverterTest {
    
    @Serializable
    data class Small(
        @FixedLength(3)
        val s: String
    )
    
    @Serializable
    data class Large(
        @FixedLength(8)
        val l: String
    )
    
    @Test
    fun smallToLarge() {
        val small = listOf(Small("Foo"), Small("Bar"))
        val format = FixedLengthFormat(lineSeparator = "")
        val smallString = format.encodeToString(ListSerializer(Small.serializer()), small)
        assertEquals("FooBar", smallString)
        val large = format.decodeFromString(Large.serializer(), smallString)
        assertEquals(Large("FooBar"), large)
    }
    
    @Test
    fun largeToSmall() {
        val large = Large("FooBarXX")
        val format = FixedLengthFormat(lineSeparator = "")
        val largeString = format.encodeToString(Large.serializer(), large)
        val smalls = format.decodeFromString(ListSerializer(Small.serializer()), largeString)
        assertEquals(listOf(Small("Foo"), Small("Bar")), smalls)
    }
}
