package app.softwork.serialization.flf

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.*
import kotlin.jvm.JvmInline
import kotlin.math.absoluteValue
import kotlin.math.min

@ExperimentalSerializationApi
internal fun <T> DeserializationStrategy<T>.parseRecordMinLength(value: CharSequence): Int {
    require(descriptor.kind == StructureKind.CLASS) {
        "Parsing requires a top level class, but was ${descriptor.kind}."
    }
    val record = descriptor.parseRecord(value, startIndex = 0)
    
    var minLength = 0
    var gotNonNull = false
    for (element in record.elements.reversed()) {
        when (element) {
            is IntPrimitive -> TODO()
            is Primitive -> {
                val length = element.length
                if (gotNonNull || length >= 0) {
                    minLength += element.length
                }
            }
            is InnerList -> TODO()
            
            is Record -> TODO()
            is SealedDiscriminator -> {
                require(element.length > 0)
                minLength += element.length
            }
        }
        minLength += element.length
    }
    
    return minLength
}

private fun Element.minLength(): Int {
    
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
    val elements: Array<Element>
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
): Array<Element> = buildList {
    var index = startIndex
    for ((elementIndex, elementDescriptor) in elementDescriptors.withIndex()) {
        val element = elementDescriptor.parse(value, index) { counterSerialName, serialName ->
            resolveReference(counterSerialName, serialName, elementIndex) { get(it) }
        }
        index += element.length.absoluteValue
        add(element)
    }
}.toTypedArray()

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
    value: CharSequence,
    startIndex: Int,
    resolve: (String, String) -> Element
): Element {
    when (kind) {
        is StructureKind.MAP -> error("Maps are not supported: $serialName")
        StructureKind.CLASS -> return parseRecord(value, startIndex)

        is PrimitiveKind -> {
            for (anno in annotations) {
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
                        value = value,
                        startIndex = startIndex + length,
                        resolve = resolve
                    )

                    return Record(
                        elements = arrayOf(
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
                    require(elementDescriptor.kind != SerialKind.CONTEXTUAL) {
                        "Inner lists requires the same kind or a sealed class, but was ${elementDescriptor.kind}"
                    }
                    var length = 0
                    repeat(counter.value) {
                        val element = elementDescriptor.parse(
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
