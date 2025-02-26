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

- Inner lists (and Maps) are not supported, eg. `data class NotSupported(val innerList: List<String>)`
- Maps are not supported.
