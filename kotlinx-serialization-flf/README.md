# Module kotlinx-serialization-flf

This module contains the [Fixed LengthFile Format](https://www.ibm.com/docs/en/psfa/7.2.1?topic=format-fixed-length-files).

## Usage

```
John Doe  
```

To decode from the given Fixed-Length string you need to apply `@FixedLength(length = n)`:

```kotlin
@Serializable
data class Names(
    @FixedLength(5) val firstName: String,
    @FixedLength(5) val lastName: String
)

FixedLengthFormat.decodeFromString(Names.serializer(), flf)
```

And to encode:

```kotlin
FixedLengthFormat.encodeToString(Names.serializer(), Names("John", "Doe"))

"""
John Doe  
"""
```

## Limitations

- The order of the properties of the class must match the order of the fields!
- All whitespaces are trimmed.
- Because this format does not have any delimiters, there is no check, if a given length is too long and consumes the
  next value.
- Because this format does not have any delimiters, it is not possible to decode/encode primitives. You must use a class
  with `@FixedLength` annotated properties.
- Inner lists are not supported, eg. `data class NotSupported(val innerList: List<String>)`
- Maps are not supported.
