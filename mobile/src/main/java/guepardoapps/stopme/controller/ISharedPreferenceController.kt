package guepardoapps.stopme.controller

interface ISharedPreferenceController {
    fun <T : Any> save(key: String, value: T)

    fun <T : Any> load(key: String, defaultValue: T): T

    fun remove(key: String)

    fun erase()
}