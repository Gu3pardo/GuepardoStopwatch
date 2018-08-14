package guepardoapps.stopme.controller

interface ISharedPreferenceController {
    fun <T : Any> save(key: String, value: T)
    fun <T : Any> load(key: String, defaultValue: T): Any
    fun remove(key: String)
    fun erase()
}