package app.softwork.serialization.csv

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*

@ExperimentalSerializationApi
internal val SerialDescriptor.flatNames: Iterator<String>
    get() = iterator {
        names(this@flatNames)
    }

@ExperimentalSerializationApi
private suspend fun SequenceScope<String>.names(descriptor: SerialDescriptor) {
    for (i in 0 until descriptor.elementsCount) {
        val elementDescriptor = descriptor.getElementDescriptor(i)
        if (elementDescriptor.elementsCount == 0 || elementDescriptor.kind == SerialKind.ENUM) {
            yield(descriptor.getElementName(i))
        } else {
            names(elementDescriptor)
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
internal fun SerialDescriptor.checkForLists() {
    for (elementDescriptor in elementDescriptors) {
        require(elementDescriptor.kind !is StructureKind.LIST) {
            error("List is not yet supported")
        }
        require(elementDescriptor.kind !is StructureKind.MAP) {
            error("Map is not yet supported")
        }
        elementDescriptor.checkForLists()
    }
}

@OptIn(ExperimentalSerializationApi::class)
internal fun SerialDescriptor.checkForPolymorphicClasses() {
    for (elementDescriptor in elementDescriptors) {
        require(elementDescriptor.kind !is PolymorphicKind) {
            "Polymorphic classes are not supported with encodeToString using encodeHeader."
        }
        elementDescriptor.checkForPolymorphicClasses()
    }
}
