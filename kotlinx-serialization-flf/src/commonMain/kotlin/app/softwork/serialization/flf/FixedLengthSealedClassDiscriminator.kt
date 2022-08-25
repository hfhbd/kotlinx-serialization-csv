package app.softwork.serialization.flf

import kotlinx.serialization.*

@ExperimentalSerializationApi
@Target(AnnotationTarget.PROPERTY)
@SerialInfo
public annotation class FixedLengthSealedClassDiscriminator(val serialName: String)
