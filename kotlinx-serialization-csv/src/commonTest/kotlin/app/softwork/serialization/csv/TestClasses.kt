package app.softwork.serialization.csv

import kotlinx.datetime.*
import kotlinx.serialization.*

@Serializable
data class Foo(val bar: Int)

@Serializable
data class FooNamed(@SerialName("foo") val bar: Int)

@Serializable
data class FooNull(val bar: Int, val baz: Int?)

@Serializable
data class FooNullFirst(val baz: Int?, val bar: Int)

@Serializable
data class FooNested(val baz: Int?, val child: FooNullFirst, val foo: Int)

@Serializable
data class FooList(val baz: Int?, val child: List<FooNullFirst>)

@Serializable
data class FooEnum(val baz: Int?, val foo: A) {
    @Serializable
    enum class A {
        One, Two, Three
    }
}

@Serializable
@JvmInline
value class FooInline(val foo: Int)

@Serializable
data class FooComplex(val bar: String?, val inline: FooInline, val enum: FooEnum.A, val instant: Instant)
