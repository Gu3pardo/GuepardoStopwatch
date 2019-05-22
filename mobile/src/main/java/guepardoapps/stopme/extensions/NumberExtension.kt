package guepardoapps.stopme.extensions

import java.util.*

/**
 * @param digits the numbers to show
 * @return returns a string with specified format and additional zeros
 */
fun Int.integerFormat(digits: Int): String = String.format(Locale.getDefault(), "%0${digits}d", this)