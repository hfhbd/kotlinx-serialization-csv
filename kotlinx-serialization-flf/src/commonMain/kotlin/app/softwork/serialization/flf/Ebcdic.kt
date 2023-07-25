package app.softwork.serialization.flf

import kotlinx.serialization.*
import kotlin.math.*

@ExperimentalSerializationApi
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
public annotation class Ebcdic(val format: Format) {
    public enum class Format {

        /**
         * The integer is converted using each digit.
         * Each digit is stored binary using 4 bits (1 nibble).
         * ZonedDecimal uses 8 bits (1 byte) or 2 nibbles.
         *
         * The leading 4 bits (leading nibble) contain the sign:
         * 1111 (F) for unsigned digit, 1100 (C) for a positive and 1101 (D) for a negative one.
         *
         * The sign of the integer is only stored in the last digit.
         *
         * 1234 (unsigned) results into xF1F2F3F4.
         * +1234 (signed)  results into xF1F2F3C4.
         * -1234 (signed)  results into xF1F2F3D4.
         *
         * The hex value is stored in EBCDIC (IBM-1047).
         */
        Zoned {
            override fun toInt(string: CharSequence): Int? = toLong(string)?.toInt()

            override fun toLong(string: CharSequence): Long? =
                string.fromSignedEBCDICZonedDecimal()

            override fun toString(value: Int): String {
                val positive = value >= 0
                val toString = value.absoluteValue.toString()
                return toString.toSignedEBCDICZonedDecimal(positive)
            }

            override fun toString(value: Long): String {
                val positive = value >= 0
                val toString = value.absoluteValue.toString()
                return toString.toSignedEBCDICZonedDecimal(positive)
            }
        };

        internal abstract fun toInt(string: CharSequence): Int?
        internal abstract fun toLong(string: CharSequence): Long?

        internal abstract fun toString(value: Int): CharSequence
        internal abstract fun toString(value: Long): CharSequence
    }
}

internal fun String.toSignedEBCDICZonedDecimal(positive: Boolean): String {
    val last = when (last()) {
        '1' -> if (positive) 'A' else 'J'
        '2' -> if (positive) 'B' else 'K'
        '3' -> if (positive) 'C' else 'L'
        '4' -> if (positive) 'D' else 'M'
        '5' -> if (positive) 'E' else 'N'
        '6' -> if (positive) 'F' else 'O'
        '7' -> if (positive) 'G' else 'P'
        '8' -> if (positive) 'H' else 'Q'
        '9' -> if (positive) 'I' else 'R'
        '0' -> if (positive) '{' else '}'
        else -> error("No digit")
    }
    val start = subSequence(0, lastIndex)
    return "$start$last"
}

private fun CharSequence.fromSignedEBCDICZonedDecimal(): Long? {
    var result = substring(0, lastIndex).toLongOrNull()?.times(10) ?: return null
    when (last()) {
        '{' -> {} // positive 0
        'A' -> result += 1
        'B' -> result += 2
        'C' -> result += 3
        'D' -> result += 4
        'E' -> result += 5
        'F' -> result += 6
        'G' -> result += 7
        'H' -> result += 8
        'I' -> result += 9
        '}' -> { // negative 0
            result *= -1
        }
        'J' -> {
            result += 1
            result *= -1
        }

        'K' -> {
            result += 2
            result *= -1
        }

        'L' -> {
            result += 3
            result *= -1
        }

        'M' -> {
            result += 4
            result *= -1
        }

        'N' -> {
            result += 5
            result *= -1
        }

        'O' -> {
            result += 6
            result *= -1
        }

        'P' -> {
            result += 7
            result *= -1
        }

        'Q' -> {
            result += 8
            result *= -1
        }

        'R' -> {
            result += 9
            result *= -1
        }
    }
    return result
}
