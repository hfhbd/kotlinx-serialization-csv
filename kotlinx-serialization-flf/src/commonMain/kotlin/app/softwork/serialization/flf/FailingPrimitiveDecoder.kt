package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.encoding.*

internal interface FailingPrimitiveDecoder : Decoder {
    override fun decodeBoolean(): Nothing =
        error("Primitives are not supported due to missing length")

    override fun decodeByte(): Nothing =
        error("Primitives are not supported due to missing length")

    override fun decodeChar(): Nothing =
        error("Primitives are not supported due to missing length")

    override fun decodeDouble(): Nothing =
        error("Primitives are not supported due to missing length")

    override fun decodeFloat(): Nothing =
        error("Primitives are not supported due to missing length")

    override fun decodeInt(): Nothing =
        error("Primitives are not supported due to missing length")

    override fun decodeLong(): Nothing =
        error("Primitives are not supported due to missing length")

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing =
        error("Primitives are not supported due to missing length")

    override fun decodeShort(): Nothing =
        error("Primitives are not supported due to missing length")

    override fun decodeString(): Nothing =
        error("Primitives are not supported due to missing length")
}
