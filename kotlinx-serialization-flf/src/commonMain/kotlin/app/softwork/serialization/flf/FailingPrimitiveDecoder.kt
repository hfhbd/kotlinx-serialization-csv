package app.softwork.serialization.flf

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encoding.Decoder

internal interface FailingPrimitiveDecoder : Decoder {
    override fun decodeBoolean() =
        error("Primitives are not supported due to missing length")

    override fun decodeByte() =
        error("Primitives are not supported due to missing length")

    override fun decodeChar() =
        error("Primitives are not supported due to missing length")

    override fun decodeDouble() =
        error("Primitives are not supported due to missing length")

    override fun decodeFloat() =
        error("Primitives are not supported due to missing length")

    override fun decodeInt() =
        error("Primitives are not supported due to missing length")

    override fun decodeLong() =
        error("Primitives are not supported due to missing length")

    @ExperimentalSerializationApi
    override fun decodeNull() =
        error("Primitives are not supported due to missing length")

    override fun decodeShort() =
        error("Primitives are not supported due to missing length")

    override fun decodeString() =
        error("Primitives are not supported due to missing length")
}
