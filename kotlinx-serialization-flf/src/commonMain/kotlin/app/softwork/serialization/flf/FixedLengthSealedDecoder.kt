package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.modules.*

@ExperimentalSerializationApi
internal class FixedLengthSealedDecoder(
    private val classDiscriminator: SealedClassClassDiscriminator,
    private val originalDecoder: FixedLengthDecoder
) : Decoder by originalDecoder, CompositeDecoder by originalDecoder {
    override val serializersModule: SerializersModule = originalDecoder.serializersModule

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        originalDecoder.beginStructure(descriptor)
        return this
    }

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        return if (index == 0) {
            when (classDiscriminator) {
                is SealedClassClassDiscriminator.Length -> originalDecoder.decode(classDiscriminator.length).toString()
                is SealedClassClassDiscriminator.Property -> classDiscriminator.classDiscriminator
            }
        } else {
            originalDecoder.decodeStringElement(descriptor, index)
        }
    }
}
