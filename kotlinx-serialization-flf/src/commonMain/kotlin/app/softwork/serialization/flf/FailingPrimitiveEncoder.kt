package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.encoding.*

internal interface FailingPrimitiveEncoder : Encoder {
    override fun encodeBoolean(value: Boolean) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeByte(value: Byte) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeChar(value: Char) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeDouble(value: Double) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeFloat(value: Float) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeInt(value: Int) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeLong(value: Long) {
        error("Primitives are not supported due to missing length")
    }

    @ExperimentalSerializationApi
    override fun encodeNull() {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeShort(value: Short) {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeString(value: String) {
        error("Primitives are not supported due to missing length")
    }
}
