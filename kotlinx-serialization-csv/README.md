# Module kotlinx-serialization-csv

This module contains the [CSV-Format](https://datatracker.ietf.org/doc/html/rfc4180).

## Usage

```csv
firstName,lastName
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

### Quotes

```csv
"lastName";"firstName"
"Doe";"John"
```

To decode from the given CSV string with quotes and unordered attributes:

```kotlin
@Serializable
data class Names(val firstName: String, val lastName: String)

CSVFormat {
    separator = ';'
    alwaysEmitQuotes = true
}.decodeFromString(Names.serializer(), csv)
```

And to encode:

```kotlin
CSVFormat {
    separator = ';'
    alwaysEmitQuotes = true
}.encodeToString(Names.serializer(), Names("John", "Doe"))

"""
"firstName";"lastName"
"John";"Doe"
"""
```

## Limitations

- Inner lists are not supported, eg. `data class NotSupported(val innerList: List<String>)`
- Maps are not supported.
