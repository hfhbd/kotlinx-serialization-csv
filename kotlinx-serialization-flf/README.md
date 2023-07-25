# Module kotlinx-serialization-flf

This module contains
the [Fixed LengthFile Format](https://www.ibm.com/docs/en/psfa/7.2.1?topic=format-fixed-length-files).

## Usage

To decode from a given Fixed-Length string, you need to apply `@FixedLength(length = n)` to each property of your class:

```kotlin
val flf = "John Doe"

@Serializable
data class Names(
    @FixedLength(5) val firstName: String,
    @FixedLength(5) val lastName: String,
    @FixedLength(3) val age: Int?
)

val john = FixedLengthFormat.decodeFromString(Names.serializer(), flf)
john.firstName // "John"
john.lastName // "Doe"
john.age // null
```

And to encode:

```kotlin
FixedLengthFormat.encodeToString(Names.serializer(), Names("John", "Doe", 42))

"""
John Doe   42
"""
0000011111222 // where 0 is firstName, 1 is lastName and 2 is the age

FixedLengthFormat.encodeToString(Names.serializer(), Names("John", "Doe", null))

"John Doe     "
```

Encoding always adds blank values for empty strings as well as for null values.

## Inline classes

Inline classes behave like regular classes, and to support [multi field value classes in the future](https://github.com/Kotlin/KEEP/pull/339), you need to annotate the properties.

```kotlin
@Serializable
data class Data(
    val myType: MyInlineClass
)

@JvmInline
value class MyInlineClass(
  @FixedLength(4)
  val s: String
)
```

## Inner lists

Inner lists require a counter.
The list needs to be annotated with `FixedLengthList(serialName = myCounter)`.
The counter needs to be a `Int`, due to Kotlin List size.

```kotlin
@Serializable
data class MyHolder(
    @FixedLength(3)
    val counter: Int,

    @FixedLengthList("counter")
    val myList: List<ListElement>
)

@Serializable
data class ListElement(
  @FixedLength(2)
  val data: Int
)

val flf = "  3010203"
val data: MyHolder = FixedLengthFormat.decodeFromString(flf)
data.counter // 3
data.myList // listOf(ListElement(01), ListElement(02), ListElement(03))
```

The type of the list elements needs to be a (sealed) class.

## Sealed classes

Sealed classes are supported if you specify the length of the type discriminator.
Either adding the `@FixedLengthSealedClassDiscriminatorLength` to the sealed class
or use the `@FixedLengthSealedClassDiscriminator` to reference to a previous member.

The `@FixedLengthSealedClassDiscriminatorLength` is only supported at classes and writes the length right before the
class.

```kotlin
@FixedLengthSealedClassDiscriminatorLength(3)
@Serializable
sealed class Seal {
    @Serializable
    data class A(
        @FixedLength(2)
        val a: Int
    ) : Seal

    @Serializable
    data class B(
        @FixedLength(4)
        val b: Int
    ) : Seal
}

val flf = "A  42\nB  4242"
val data: List<Seal> = FixedLengthFormat.decodeFromString(data)
```

If you need to have other types between the discriminator and the sealed class, use
`@FixedLengthSealedClassDiscriminator` which references a previous `Char` or `String` element.

```kotlin
@Serializable
data class Complex(
    @SerialName("type")
    @FixedLength(4)
    val myType: String,

    @FixedLength(2)
    val counter: Int,

    @FixedLengthList("counter")
    @FixedLengthSealedClassDiscrimiator("type")
    val elements: List<Element>
)

@Serializable
sealed class Element

@Serializable
data class Foo(
    @FixedLength(4)
    val b: Int
) : Element
```

## Limitations

- The order of the properties of the class must match the order of the fields!
- Because this format does not have any delimiters, there is no check, if a given length is too long and consumes the
  next value.
- Because this format does not have any delimiters, it is not possible to decode/encode primitives. You must use a class
  with `@FixedLength` annotated properties.
- Maps are not supported.
