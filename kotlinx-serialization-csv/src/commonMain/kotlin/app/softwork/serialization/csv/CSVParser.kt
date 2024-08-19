package app.softwork.serialization.csv

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlin.jvm.JvmInline

internal sealed interface CSVNode {
    @JvmInline
    value class Element(val value: String) : CSVNode
    data object NewLine : CSVNode
}

internal fun String.parse(
    separator: Char = ',',
    lineSeparator: String = "\n",
): StatefulIterator<CSVNode> = iterator {
    var index = 0

    main@ while (true) {
        if (index >= length) {
            return@iterator
        }

        val start = index
        when (getOrNull(index)) {
            null -> return@iterator

            separator -> {
                yield(CSVNode.Element(""))
                index += 1
            }

            else -> {
                normal@ while (true) {
                    val currentChar = getOrNull(index)
                    when {
                        currentChar == '"' -> {
                            var indexOfClosingQuotes = index + 1
                            escaping@ while (true) {
                                val nextChar = getOrNull(indexOfClosingQuotes)
                                when (nextChar) {
                                    '"' -> {
                                        if (indexOfClosingQuotes == lastIndex) {
                                            val text = substring(start + 1, indexOfClosingQuotes).replace(
                                                oldValue = "\"\"",
                                                newValue = "\""
                                            )
                                            val node = CSVNode.Element(text)
                                            yield(node)
                                            return@iterator
                                        } else {
                                            val following = get(indexOfClosingQuotes + 1)
                                            if (following == '"') {
                                                indexOfClosingQuotes += 1
                                            } else {
                                                index = indexOfClosingQuotes + 1
                                                break@escaping
                                            }
                                        }
                                    }

                                    null -> throw SerializationException("Missing end of quotes at ${indexOfClosingQuotes - 1}")
                                }
                                indexOfClosingQuotes += 1
                            }
                        }

                        currentChar == null -> {
                            val node = CSVNode.Element(substring(start, index))
                            yield(node)
                            break@main
                        }

                        currentChar == separator -> {
                            val text = if (get(start) == '"' && get(index - 1) == '"') {
                                substring(start + 1, index - 1).replace(oldValue = "\"\"", newValue = "\"")
                            } else {
                                substring(start, index)
                            }
                            val node = CSVNode.Element(text)
                            yield(node)
                            index += 1
                            break@normal
                        }

                        currentChar == lineSeparator.first() && substring(index until index + lineSeparator.length) == lineSeparator -> {
                            val text = if (get(start) == '"' && get(index - 1) == '"') {
                                substring(start + 1, index - 1).replace(oldValue = "\"\"", newValue = "\"")
                            } else {
                                substring(start, index)
                            }
                            val node = CSVNode.Element(text)
                            yield(node)
                            index += lineSeparator.length
                            yield(CSVNode.NewLine)
                            break@normal
                        }

                        else -> index += 1
                    }
                }
            }
        }
    }
}.stateful()

internal fun Iterator<CSVNode>.getHeader(): List<String> = buildList {
    for (node in this@getHeader) {
        when (node) {
            is CSVNode.Element -> add(node.value)
            CSVNode.NewLine -> break
        }
    }
}

@ExperimentalSerializationApi
internal fun List<String>.isSequentially(descriptor: SerialDescriptor): Boolean {
    if (descriptor.kind is StructureKind.CLASS) {
        for ((index, expectedName) in descriptor.flatNames.withIndex()) {
            val actualName = getOrNull(index)
            if (actualName == null) {
                return false
            }
            if (actualName != expectedName) {
                return false
            }
        }

        return true
    } else {
        return false
    }
}
