package app.softwork.serialization.flf

import kotlinx.datetime.*
import kotlinx.serialization.*
import kotlin.jvm.*

@ExperimentalSerializationApi
@Serializable
data class Sample(
    @FixedLength(5) val shortString: String,
    @FixedLength(10) val longString: String,
    @FixedLength(4) val int: Int,
    @FixedLength(4) val double: Double,
    @FixedLength(4) val nil: Double?,
    @FixedLength(20) val date: Instant,
    @FixedLength(5) val enum: Testing,
    @FixedLength(3) val inline: Foo,
    @FixedLength(3) val inlineS: FooS,
    @FixedLength(3) val inlineD: FooD,
    @FixedLength(4) val inlineB: FooB,
    @FixedLength(3) val inlineL: FooL,
    @FixedLength(1) val inlineChar: FooChar,
    @FixedLength(1) val inlineShort: FooShort,
    @FixedLength(3) val inlineFloat: FooFloat,
    @FixedLength(1) val inlineByte: FooByte,
    val innerClass: Inner,

    @FixedLength(5) val boolean: Boolean,
    @FixedLength(4) val byte: Byte,
    @FixedLength(4) val short: Short,
    @FixedLength(4) val float: Float,
    @FixedLength(4) val long: Long,
    @FixedLength(1) val char: Char
) {
    @Serializable
    enum class Testing {
        One, Two, Three
    }

    @Serializable
    data class Inner(
        @FixedLength(1) val s: Int
    )

    @Serializable
    @JvmInline
    value class Foo(val int: Int)

    @Serializable
    @JvmInline
    value class FooS(val int: String)

    @Serializable
    @JvmInline
    value class FooD(val int: Double)

    @Serializable
    @JvmInline
    value class FooB(val int: Boolean)

    @Serializable
    @JvmInline
    value class FooL(val int: Long)

    @Serializable
    @JvmInline
    value class FooShort(val int: Short)

    @Serializable
    @JvmInline
    value class FooByte(val int: Byte)

    @Serializable
    @JvmInline
    value class FooChar(val int: Char)

    @Serializable
    @JvmInline
    value class FooFloat(val int: Float)
}

@ExperimentalSerializationApi
@Serializable
data class Small(
    @FixedLength(1) val s: String?
)

@Serializable
data class Missing(val s: Int)

@Serializable
data class InnerList(val s: List<Int>)
