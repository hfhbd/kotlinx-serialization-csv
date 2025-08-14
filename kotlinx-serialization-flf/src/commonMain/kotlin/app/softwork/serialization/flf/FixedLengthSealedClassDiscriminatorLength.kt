package app.softwork.serialization.flf

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@ExperimentalSerializationApi
@Target(AnnotationTarget.CLASS)
@SerialInfo
public annotation class FixedLengthSealedClassDiscriminatorLength(val length: Int)
