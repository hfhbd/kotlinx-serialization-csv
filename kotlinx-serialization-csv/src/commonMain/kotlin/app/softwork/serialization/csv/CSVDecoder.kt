package app.softwork.serialization.csv

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*

@ExperimentalSerializationApi
internal class CSVDecoder(
    private val data: List<List<String>>,
    private val maxIndex: Int,
    override val serializersModule: SerializersModule
) : AbstractDecoder() {

    private var index = 0
    private var parentIndex = 0
    private var currentIndex = 0
    private var currentMaxIndex: Int? = null
    private var currentRow = 0

    override fun beginStructure(descriptor: SerialDescriptor): CSVDecoder {
        currentMaxIndex = Iterable { descriptor.flatNames }.count()
        parentIndex = currentIndex
        currentIndex = 0
        return this
    }

    override fun decodeNotNullMark(): Boolean = data[currentRow][index] != ""

    override fun endStructure(descriptor: SerialDescriptor) {
        currentIndex = parentIndex
        if (index == maxIndex) {
            currentRow++
            index = 0
        }
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (currentIndex == currentMaxIndex) return CompositeDecoder.DECODE_DONE
        return currentIndex++
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor) = data.size
    override fun decodeSequentially(): Boolean = true

    override fun decodeNull(): Nothing? {
        index++
        return null
    }

    override fun decodeBoolean() = decodeString().toBoolean()

    override fun decodeByte() = decodeString().toByte()

    override fun decodeShort() = decodeString().toShort()

    override fun decodeInt() = decodeString().toInt()

    override fun decodeLong() = decodeString().toLong()

    override fun decodeFloat() = decodeString().toFloat()

    override fun decodeDouble() = decodeString().toDouble()

    override fun decodeChar() = decodeString().single()

    override fun decodeString() = data[currentRow][index].also {
        index++
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor) = enumDescriptor.elementNames.indexOf(decodeString())
}
