package app.softwork.serialization.flf

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.*
import kotlin.jvm.JvmInline
import kotlin.math.absoluteValue
import kotlin.math.min

@ExperimentalSerializationApi
public fun SerialDescriptor.parseRecordMinLength(value: CharSequence): Int {
    require(kind == StructureKind.CLASS) {
        "Parsing requires a top level class, but was ${kind}."
    }
    val record = parseRecord(value, startIndex = 0)
    return record.minLength()
}

private fun Record.minLength(): Int {
    var minLength = 0
    var gotNonNull = false
    for (element in elements.reversed()) {
        when (element) {
            is IntPrimitive -> {
                if (element.length < 0) {
                    gotNonNull = true
                }
                if (gotNonNull) {
                    minLength += element.length.absoluteValue
                }
            }
            is Primitive -> {
                if (element.length < 0) {
                    gotNonNull = true
                }
                if (gotNonNull) {
                    minLength += element.length.absoluteValue
                }
            }
            is InnerList -> {
                minLength += element.length
            }

            is Record -> {
                val recordLength = element.minLength()
                if (recordLength < 0) {
                    gotNonNull = true
                }
                if (gotNonNull) {
                    minLength += recordLength.absoluteValue
                }
            }
            is SealedDiscriminator -> {
                require(element.length > 0)
                minLength += element.length
                gotNonNull = true
            }
        }
        minLength += element.length
    }

    return minLength
}

@ExperimentalSerializationApi
private fun SerialDescriptor.parseRecord(value: CharSequence, startIndex: Int): Record {
    val elements = parseElements(value, startIndex)

    return Record(
        elements = elements
    )
}

private sealed interface Element {
    // negative value means element is nullable
    val length: Int
}

@JvmInline
private value class Record(
    val elements: List<Element>
) : Element {
    override val length: Nothing
        get() = error("Not supported")
}

@JvmInline
private value class Primitive(override val length: Int) : Element

@JvmInline
private value class SealedDiscriminator(override val length: Int) : Element

private class IntPrimitive(
    override val length: Int,
    // -1 means nullable
    val value: Int
) : Element

@JvmInline
private value class InnerList(override val length: Int) : Element

@ExperimentalSerializationApi
private fun SerialDescriptor.parseElements(
    value: CharSequence,
    startIndex: Int
): List<Element> = buildList {
    var index = startIndex
    for ((elementIndex, elementDescriptor) in elementDescriptors.withIndex()) {
        val elementAnnotations = getElementAnnotations(elementIndex)
        val element = elementDescriptor.parse(elementAnnotations, value, index) { counterSerialName, serialName ->
            resolveReference(counterSerialName, serialName, elementIndex) { get(it) }
        }
        index += element.length.absoluteValue
        add(element)
    }
}

@ExperimentalSerializationApi
private fun SerialDescriptor.resolveReference(
    counterSerialName: String,
    listName: String,
    elementIndex: Int,
    get: (Int) -> Element
): Element {
    val counterIndex = getElementIndex(counterSerialName)
    val refElement = get(counterIndex)
    require(counterIndex < elementIndex) {
        "$counterSerialName needs be written before the $listName"
    }
    return refElement
}

@ExperimentalSerializationApi
private fun SerialDescriptor.parse(
    elementAnnotations: Iterable<Annotation>,
    value: CharSequence,
    startIndex: Int,
    resolve: (String, String) -> Element
): Element {
    when (kind) {
        is StructureKind.MAP -> error("Maps are not supported: $serialName")
        StructureKind.CLASS -> return parseRecord(value, startIndex)

        is PrimitiveKind -> {
            for (anno in elementAnnotations) {
                if (anno is FixedLength) {
                    val length = if (isNullable) {
                        anno.length * -1
                    } else anno.length
                    return if (kind is PrimitiveKind.INT) {
                        val intValue = if (isNullable) {
                            val maxIndex = min(value.length, length)
                            value.substring(startIndex, maxIndex).toIntOrNull() ?: -1
                        } else {
                            value.substring(startIndex, length).toInt()
                        }
                        IntPrimitive(
                            length = length,
                            value = intValue
                        )
                    } else Primitive(length)
                }
            }
            error("No @FixedLength found at $serialName")
        }

        PolymorphicKind.SEALED -> {
            for (anno in annotations) {
                if (anno is FixedLengthSealedClassDiscriminatorLength) {
                    val length = anno.length

                    val elementDescriptor = elementDescriptors.single()

                    val element = elementDescriptor.parse(
                        elementAnnotations = getElementAnnotations(0),
                        value = value,
                        startIndex = startIndex + length,
                        resolve = resolve
                    )

                    return Record(
                        elements = listOf(
                            SealedDiscriminator(length),
                            element
                        )
                    )
                }
            }
            return parseRecord(value, startIndex)
        }

        is StructureKind.LIST -> {
            for (anno in annotations) {
                if (anno is FixedLengthList) {
                    val counter = resolve(anno.serialName, serialName)
                    require(counter is IntPrimitive) {
                        "$counter needs to be a Int."
                    }
                    val elementDescriptor = elementDescriptors.single()
                    require(!elementDescriptor.isNullable) {
                        "Inner lists can't have nullable types."
                    }
                    require(elementDescriptor.kind != SerialKind.CONTEXTUAL) {
                        "Inner lists requires the same kind or a sealed class, but was ${elementDescriptor.kind}"
                    }
                    var length = 0
                    repeat(counter.value) {
                        val element = elementDescriptor.parse(
                            elementAnnotations = getElementAnnotations(0),
                            value = value,
                            startIndex = startIndex + length,
                            resolve = resolve
                        )
                        length += element.length
                    }
                    return InnerList(length)
                }
            }
            error("No @FixedLengthList found at $serialName")
        }

        else -> error("Not supported: $serialName of $kind")
    }
}
