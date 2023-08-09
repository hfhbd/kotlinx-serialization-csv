package app.softwork.serialization.flf

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
internal class FLFTopLevelListDecoder(
    override val serializersModule: SerializersModule,
    val collectionSize: Int,
    val hasNextRecord: (SerialDescriptor) -> Boolean,
) : CompositeDecoder {

    override fun decodeSequentially(): Boolean = collectionSize != -1
    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = collectionSize

    override fun endStructure(descriptor: SerialDescriptor) {}

    private var currentElementIndex = -1
    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentElementIndex += 1

        return if (hasNextRecord(descriptor.getElementDescriptor(0))) {
            currentElementIndex
        } else DECODE_DONE
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        TODO("Not yet implemented")
    }
}
