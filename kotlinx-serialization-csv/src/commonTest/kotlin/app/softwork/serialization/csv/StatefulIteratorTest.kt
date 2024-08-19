package app.softwork.serialization.csv

import kotlin.test.Test
import kotlin.test.assertEquals

class StatefulIteratorTest {

    @Test
    fun normal() {
        val iter = iterator {
            yield(1)
            yield(2)
            yield(3)
            yield(4)
        }.stateful()

        assertEquals(null, iter.current)

        assertEquals(1, iter.peek())
        assertEquals(1, iter.next())
        assertEquals(1, iter.current)

        assertEquals(2, iter.peek())
        assertEquals(2, iter.peek())
        assertEquals(2, iter.next())
        assertEquals(2, iter.current)

        assertEquals(3, iter.peek())
        assertEquals(3, iter.next())
        assertEquals(3, iter.current)

        assertEquals(4, iter.next())
        assertEquals(4, iter.current)


        assertEquals(null, iter.peek())
        assertEquals(false, iter.hasNext())
        assertEquals(null, iter.current)
    }

    @Test
    fun asList() {
        val iter = iterator {
            yield(1)
            yield(2)
            yield(3)
        }.stateful()

        assertEquals(listOf(1, 2, 3), iter.asSequence().toList())
    }

    @Test
    fun empty() {
        val iter = iterator<Int> {}.stateful()

        assertEquals(null, iter.peek())
        assertEquals(false, iter.hasNext())

        assertEquals(listOf(), iter.asSequence().toList())
    }
}
