package guepardoapps.stopme.controller

import android.content.Context
import com.andreacioccarelli.cryptoprefs.CryptoPrefs
import guepardoapps.stopme.common.Constants
import guepardoapps.stopme.utils.Logger

class SharedPreferenceController(context: Context) : ISharedPreferenceController {
    private val tag: String = SharedPreferenceController::class.java.simpleName

    private val cryptoPrefs: CryptoPrefs = CryptoPrefs(context, Constants.sharedPrefName, Constants.sharedPrefKey)

    override fun <T : Any> save(key: String, value: T) {
        when (value::class) {
            Boolean::class -> cryptoPrefs.put(key, value as Boolean)
            Byte::class -> cryptoPrefs.put(key, value as Byte)
            Double::class -> cryptoPrefs.put(key, value as Double)
            Float::class -> cryptoPrefs.put(key, value as Float)
            Int::class -> cryptoPrefs.put(key, value as Int)
            Long::class -> cryptoPrefs.put(key, value as Long)
            Short::class -> cryptoPrefs.put(key, value as Short)
            String::class -> cryptoPrefs.put(key, value as String)
            else -> {
                Logger.instance.error(tag, "Invalid generic type of $value")
            }
        }
    }

    override fun <T : Any> load(key: String, defaultValue: T): Any {
        return when (defaultValue::class) {
            Boolean::class -> cryptoPrefs.getBoolean(key, defaultValue as Boolean)
            Byte::class -> cryptoPrefs.getByte(key, defaultValue as Byte)
            Double::class -> cryptoPrefs.getDouble(key, defaultValue as Double)
            Float::class -> cryptoPrefs.getFloat(key, defaultValue as Float)
            Int::class -> cryptoPrefs.getInt(key, defaultValue as Int)
            Long::class -> cryptoPrefs.getLong(key, defaultValue as Long)
            Short::class -> cryptoPrefs.getShort(key, defaultValue as Short)
            String::class -> cryptoPrefs.getString(key, defaultValue as String)
            else -> {
                Logger.instance.error(tag, "Invalid generic type of $defaultValue")
                return {}
            }
        }
    }

    override fun remove(key: String) {
        cryptoPrefs.remove(key)
    }

    override fun erase() {
        cryptoPrefs.erase()
    }
}