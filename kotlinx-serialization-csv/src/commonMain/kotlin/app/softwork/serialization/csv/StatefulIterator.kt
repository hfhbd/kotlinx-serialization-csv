package app.softwork.serialization.csv

internal interface StatefulIterator<out T : Any> : Iterator<T> {
    val current: T?

    fun peek(): T?
}

internal fun <T : Any> Iterator<T>.stateful(): StatefulIterator<T> = object : StatefulIterator<T> {
    override var current: T? = null
    private var peeked: T? = null

    override fun hasNext(): Boolean {
        if (peeked != null) {
            return true
        } else {
            val hasNext = this@stateful.hasNext()
            if (!hasNext) {
                current = null
            }
            return hasNext
        }
    }

    override fun next(): T {
        val peeked = peeked
        if (peeked == null) {
            val next = this@stateful.next()
            current = next
            return next
        } else {
            this.peeked = null
            current = peeked
            return peeked
        }
    }

    override fun peek(): T? {
        val peeked = peeked
        if (peeked != null) {
            return peeked
        } else if (this@stateful.hasNext()) {
            val next = this@stateful.next()
            this.peeked = next
            return next
        } else {
            return null
        }
    }
}
