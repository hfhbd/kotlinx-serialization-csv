package app.softwork.serialization.flf

import kotlinx.serialization.*

@ExperimentalSerializationApi
@Target(AnnotationTarget.CLASS)
@SerialInfo
public annotation class FixedLengthSealedType(val length: Int)
