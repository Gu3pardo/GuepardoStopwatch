package guepardoapps.stopme.logging

import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import com.github.guepardoapps.kulid.ULID
import java.sql.Date
import java.util.*

internal class Logger private constructor() {
    private var loggingEnabled: Boolean = true
    private var writeToDatabaseEnabled: Boolean = true
    private var dbHandler: DbLogging? = null

    private object Holder {
        val instance: Logger = Logger()
    }

    companion object {
        val instance: Logger by lazy { Holder.instance }
    }

    fun initialize(context: Context) {
        if (dbHandler != null) {
            return
        }
        dbHandler = DbLogging(context)
    }

    fun <T> debug(@NonNull tag: String, @NonNull description: T) {
        if (dbHandler != null && loggingEnabled) {
            Log.d(tag, description.toString())
            tryToWriteToDatabase(tag, description, Severity.Debug)
        }
    }

    fun <T> error(@NonNull tag: String, @NonNull description: T) {
        if (dbHandler != null && loggingEnabled) {
            Log.e(tag, description.toString())
            tryToWriteToDatabase(tag, description, Severity.Error)
        }
    }

    private fun <T> tryToWriteToDatabase(@NonNull tag: String, @NonNull description: T, severity: Severity) {
        if (dbHandler != null && writeToDatabaseEnabled) {
            dbHandler?.addLog(
                    DbLog(ULID.random(),
                            Date(Calendar.getInstance().timeInMillis),
                            severity,
                            tag,
                            description.toString()))
        }
    }
}