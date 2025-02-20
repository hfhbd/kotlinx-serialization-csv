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
    private val data: List<List<String>>,
    public override val configuration: CSVConfiguration,
) : AbstractDecoder(), CSVDecoder {

    override val serializersModule: SerializersModule get() = configuration.serializersModule

    private var index = 0
    private var level = 0
    private var currentRow = 0

    override fun beginStructure(descriptor: SerialDescriptor): CSVDecoder {
        if (descriptor.kind !is StructureKind.LIST) {
            level += 1
        }
        return this
    }

    override fun decodeNotNullMark(): Boolean {
        val value = data[currentRow].getOrNull(index)
        return value != null && value != ""
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        level -= 1
        if (level == 0) {
            currentRow += 1
            index = 0
        }
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        error("Never called, because decodeSequentially returns true")
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = data.size
    override fun decodeSequentially(): Boolean = true

    override fun decodeNull(): Nothing? {
        index += 1
        return null
    }

    override fun decodeBoolean(): Boolean = decodeString().toBoolean()

    override fun decodeByte(): Byte = decodeString().toByte()

    override fun decodeShort(): Short = decodeString().toShort()

    override fun decodeInt(): Int = decodeString().toInt()

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
        val value = data[currentRow].getOrNull(index) ?: throw SerializationException(
            "Missing value at index $index in line ${currentRow + READABLE_LINE_NUMBER + HEADER_OFFSET}"
        )
        index += 1
        return value
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = enumDescriptor.elementNames.indexOf(decodeString())
}

private const val READABLE_LINE_NUMBER = 1
private const val HEADER_OFFSET = 1
