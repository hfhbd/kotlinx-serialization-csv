package app.softwork.serialization.csv

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*

public interface CSVDecoder : Decoder, CompositeDecoder {
    public val configuration: CSVConfiguration
}

@ExperimentalSerializationApi
internal class CSVDecoderImpl(
    private val header: List<String>,
    private val nodes: StatefulIterator<CSVNode>,
    override val configuration: CSVConfiguration,
    val decodesSequentially: Boolean,
    private val level: Int,
) : AbstractDecoder(), CSVDecoder {

    private var index = 0
    private var currentRow = 0

    override val serializersModule: SerializersModule get() = configuration.serializersModule

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        when (descriptor.kind) {
            StructureKind.CLASS -> {
                val (nextHeaders, isSequentially) = if (configuration.includeHeader) {
                    val nextHeaders = header.subList(index, header.size)
                    nextHeaders to nextHeaders.isSequentially(descriptor)
                } else {
                    header to true
                }
                return CSVDecoderImpl(
                    header = nextHeaders,
                    nodes = nodes,
                    configuration = configuration,
                    decodesSequentially = isSequentially,
                    level = level + 1,
                )
            }

            PolymorphicKind.SEALED -> {
                return CSVDecoderImpl(
                    header = header,
                    nodes = nodes,
                    configuration = configuration,
                    decodesSequentially = true,
                    level = level,
                )
            }

            else -> {
                return CSVDecoderImpl(
                    header = header,
                    nodes = nodes,
                    configuration = configuration,
                    decodesSequentially = false,
                    level = level,
                )
            }
        }
    }

    override fun decodeNotNullMark(): Boolean {
        val isNotNull = when (val nextValue = nodes.peek()) {
            is CSVNode.Element -> nextValue.value.isNotEmpty()
            CSVNode.NewLine -> false
            null -> false
        }
        return isNotNull
    }

    override tailrec fun endStructure(descriptor: SerialDescriptor) {
        val next = nodes.peek()
        when (next) {
            is CSVNode.Element -> {
                if (level == 0) {
                    nodes.next()
                    endStructure(descriptor)
                }
            }

            CSVNode.NewLine -> nodes.next()
            null -> {}
        }
    }

    override fun decodeNull(): Nothing? {
        when (nodes.peek()) {
            is CSVNode.Element -> nodes.next()
            CSVNode.NewLine -> Unit
            null -> Unit
        }
        index += 1
        return null
    }

    override fun decodeBoolean(): Boolean = decodeString().toBoolean()

    override fun decodeByte(): Byte = decodeString().toByte()

    override fun decodeShort(): Short = decodeString().toShort()

    override fun decodeInt(): Int {
        val s = decodeString()
        return s.toInt()
    }

    override fun decodeLong(): Long = decodeString().toLong()

    override fun decodeFloat(): Float = decodeNumber().toFloat()

    private fun decodeNumber(): String {
        val data = decodeString()
        return when (configuration.numberFormat) {
            CSVFormat.NumberFormat.Dot -> data
            CSVFormat.NumberFormat.Comma -> data.replace(",", ".")
        }
    }

    override fun decodeDouble(): Double = decodeNumber().toDouble()

    override fun decodeChar(): Char = decodeString().single()

    override fun decodeString(): String {
        index += 1
        return if (nodes.peek() == null) {
            ""
        } else {
            when (val nextNode = nodes.next()) {
                is CSVNode.Element -> nextNode.value
                CSVNode.NewLine -> throwUnknownValue()
            }
        }
    }

    private fun throwUnknownValue(): Nothing = throw SerializationException(
        "Missing value at the end of line ${currentRow + READABLE_LINE_NUMBER + HEADER_OFFSET}"
    )

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = enumDescriptor.elementNames.indexOf(decodeString())

    override fun decodeSequentially(): Boolean {
        val r = decodesSequentially
        return r
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (descriptor.kind is StructureKind.LIST) {
            val nextValue = nodes.peek()
            return if (nextValue == null) {
                CompositeDecoder.DECODE_DONE
            } else {
                currentRow++
            }
        } else {
            val headerName = header.getOrNull(index) ?: return CompositeDecoder.DECODE_DONE
            val index = descriptor.getElementIndex(headerName)
            if (index == CompositeDecoder.UNKNOWN_NAME) {
                throwUnknownValue()
            }
            return index
        }
    }

    override fun <T : Any> decodeNullableSerializableValue(deserializer: DeserializationStrategy<T?>): T? {
        return super<AbstractDecoder>.decodeNullableSerializableValue(deserializer)
    }
}

private const val READABLE_LINE_NUMBER = 1
private const val HEADER_OFFSET = 1
