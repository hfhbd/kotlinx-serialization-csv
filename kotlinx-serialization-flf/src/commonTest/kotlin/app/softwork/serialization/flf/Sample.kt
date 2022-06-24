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
    companion object {
        val simple = Sample(
            shortString = "Short",
            longString = "Long",
            int = 42,
            double = 42.3,
            nil = null,
            date = Instant.fromEpochSeconds(0L),
            enum = Sample.Testing.Two,
            inline = Foo(1),
            inlineS = FooS("foo"),
            inlineD = FooD(4.2),
            inlineB = FooB(true),
            inlineL = FooL(0L),
            inlineChar = FooChar('f'),
            inlineShort = FooShort(4.toShort()),
            inlineFloat = FooFloat(1.1f),
            inlineByte = FooByte(1.toByte()),
            innerClass = Inner(8),
            boolean = false,
            byte = 1.toByte(),
            short = 1.toShort(),
            float = 4.2f,
            long = -1L,
            char = ' '
        )
    }

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

@ExperimentalSerializationApi
@Serializable
data class InnerList(
    @FixedLength(1) val count: Int,
    @FixedLength(3) val foo: String,
    @FixedLengthList("count") val s: List<Seal>
)

@ExperimentalSerializationApi
@Serializable
data class InnerListFailing(
    @FixedLength(3) val foo: String,
    @FixedLengthList("count") val s: List<Seal>,
    @FixedLength(1) val count: Int
)

@ExperimentalSerializationApi
@Serializable
@FixedLengthSealedType(1)
sealed class Seal {
    abstract val s: Int

    @SerialName("A")
    @ExperimentalSerializationApi
    @Serializable
    data class A(
        @FixedLength(2)
        val a: Int,
        @FixedLength(4)
        override val s: Int
    ) : Seal()

    @SerialName("B")
    @ExperimentalSerializationApi
    @Serializable
    data class B(
        @FixedLength(10)
        val b: String,
        @FixedLength(4)
        override val s: Int
    ) : Seal()
}
