package guepardoapps.stopme.controller

import android.content.Context
import com.andreacioccarelli.cryptoprefs.CryptoPrefs
import guepardoapps.stopme.common.Constants
import guepardoapps.stopme.logging.Logger

class SharedPreferenceController(context: Context) : ISharedPreferenceController {
    private val tag: String = SharedPreferenceController::class.java.simpleName

    private val cryptoPrefs: CryptoPrefs = CryptoPrefs(context, Constants.sharedPrefName, Constants.sharedPrefKey)

    @ExperimentalUnsignedTypes
    override fun <T : Any> save(key: String, value: T) {
        when (value::class) {
            Boolean::class -> cryptoPrefs.put(key, value as Boolean)
            Byte::class -> cryptoPrefs.put(key, value as Byte)
            UByte::class -> cryptoPrefs.put(key, value as UByte)
            Double::class -> cryptoPrefs.put(key, value as Double)
            Float::class -> cryptoPrefs.put(key, value as Float)
            Int::class -> cryptoPrefs.put(key, value as Int)
            UInt::class -> cryptoPrefs.put(key, value as UInt)
            Long::class -> cryptoPrefs.put(key, value as Long)
            ULong::class -> cryptoPrefs.put(key, value as ULong)
            Short::class -> cryptoPrefs.put(key, value as Short)
            UShort::class -> cryptoPrefs.put(key, value as UShort)
            String::class -> cryptoPrefs.put(key, value as String)
            else -> {
                Logger.instance.error(tag, "Invalid generic type of $value")
            }
        }
    }

    override fun <T : Any> load(key: String, defaultValue: T): T = cryptoPrefs.get(key, defaultValue)

    override fun remove(key: String) = cryptoPrefs.remove(key)

    override fun erase() = cryptoPrefs.erase()
}