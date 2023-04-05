# Module kotlinx-serialization-csv

This module contains the [CSV-Format](https://datatracker.ietf.org/doc/html/rfc4180).

## Usage

```
FirstName,LastName
John,Doe
```

To decode from the given CSV string:

```kotlin
@Serializable
data class Names(val firstName: String, val lastName: String)

CSVFormat.decodeFromString(Names.serializer(), csv)
```

And to encode:

```kotlin
CSVFormat.encodeToString(Names.serializer(), Names("John", "Doe"))

"""
firstName,lastName
John,Doe
"""
```

## Limitations

- The order of the properties of the class must match the order of the header/fields!
- The header is ignored.
- Inner lists are not supported, eg. `data class NotSupported(val innerList: List<String>)`
