package app.softwork.serialization.csv

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*

@ExperimentalSerializationApi
internal val SerialDescriptor.flatNames: Iterator<String>
    get() = iterator {
        names(this@flatNames)
    }

@ExperimentalSerializationApi
private suspend fun SequenceScope<String>.names(s: SerialDescriptor) {
    val count = s.elementsCount
    for (i in 0 until count) {
        val descriptor = s.getElementDescriptor(i)
        if (descriptor.elementsCount == 0 || descriptor.kind == SerialKind.ENUM) {
            yield(s.getElementName(i))
        } else {
            names(descriptor)
        }
    }
}

@ExperimentalSerializationApi
internal fun SerialDescriptor.checkForLists() {
    for (descriptor in elementDescriptors) {
        if (descriptor.kind is StructureKind.LIST || descriptor.kind is StructureKind.MAP) {
            error("List or Map are not yet supported")
        }
        descriptor.checkForLists()
    }
}
