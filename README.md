# Module kotlinx-serialization-csv

Serialize and deserialize ordered CSV and Fixed Length Format Files with kotlinx-serialization.

- [Source code](https://github.com/hfhbd/kotlinx-serialization-csv)
- [Docs](https://csv.softwork.app)

## Install

This package is uploaded to MavenCentral and supports JVM, JS(IR) and all native targets as well.

````kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("app.softwork:kotlinx-serialization-csv:LATEST")
    implementation("app.softwork:kotlinx-serialization-flf:LATEST")
}
````

## License

Apache 2

# Package app.softwork.serialization.csv

This package contains the [CSV-Format](https://datatracker.ietf.org/doc/html/rfc4180).

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

# Package app.softwork.serialization.flf

This package contains the [Fixed LengthFile Format](https://www.ibm.com/docs/en/psfa/7.2.1?topic=format-fixed-length-files).

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

- The order of the properties of the class must match the order of the header/fields!
- All whitespaces are trimmed.
- Because this format does not have any delimiters, there is no check, if a given length is too long and consumes the
  next value.
- Because this format does not have any delimiters, it is not possible to decode/encode primitives. You must use a class
  with `@FixedLength` annotated properties.
- Inner lists are not supported, eg. `data class NotSupported(val innerList: List<String>)`
