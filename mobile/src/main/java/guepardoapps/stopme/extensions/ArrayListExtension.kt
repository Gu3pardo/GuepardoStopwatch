package guepardoapps.stopme.extensions

fun <T> ArrayList<T>.replaceLast(value: T) {
    val lastIndex = this.size - 1
    if (lastIndex > -1) {
        this[lastIndex] = value
    }
}