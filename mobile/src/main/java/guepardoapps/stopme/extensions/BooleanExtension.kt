package guepardoapps.stopme.extensions

fun Boolean.toInteger(): Int {
    if (this) {
        return 1
    }
    return 0
}