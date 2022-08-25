package app.softwork.serialization.flf

internal sealed interface SealedClassClassDiscriminator {
    data class Property(val classDiscriminator: String) : SealedClassClassDiscriminator
    data class Length(val length: Int) : SealedClassClassDiscriminator
}
