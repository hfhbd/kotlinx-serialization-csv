package app.softwork.serialization.flf

import kotlinx.serialization.*

@ExperimentalSerializationApi
@Target(AnnotationTarget.PROPERTY)
@SerialInfo
public annotation class FixedLengthList(val parameterName: String)
