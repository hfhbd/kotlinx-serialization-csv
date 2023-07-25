package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlinx.serialization.encoding.*

internal interface FailingPrimitiveEncoder : Encoder {
    override fun encodeBoolean(value: Boolean): Nothing {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeByte(value: Byte): Nothing {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeChar(value: Char): Nothing {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeDouble(value: Double): Nothing {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeFloat(value: Float): Nothing {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeInt(value: Int): Nothing {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeLong(value: Long): Nothing {
        error("Primitives are not supported due to missing length")
    }

    @ExperimentalSerializationApi
    override fun encodeNull(): Nothing {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeShort(value: Short): Nothing {
        error("Primitives are not supported due to missing length")
    }

    override fun encodeString(value: String): Nothing {
        error("Primitives are not supported due to missing length")
    }
}
