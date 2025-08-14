package app.softwork.serialization.flf

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

@ExperimentalSerializationApi
@Target(AnnotationTarget.PROPERTY)
@SerialInfo
public annotation class FixedLengthList(val serialName: String)
