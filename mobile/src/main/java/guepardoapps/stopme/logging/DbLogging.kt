package guepardoapps.stopme.logging

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context

// Helpful
// https://developer.android.com/training/data-storage/sqlite
// https://www.techotopia.com/index.php/A_Kotlin_Android_SQLite_Database_Tutorial
// https://github.com/cbeust/kotlin-android-example/blob/master/app/src/main/kotlin/com/beust/example/DbHelper.kt

internal class DbLogging(context: Context)
    : SQLiteOpenHelper(context, DatabaseName, null, DatabaseVersion) {

    override fun onCreate(database: SQLiteDatabase) {
        val createTable = (
                "CREATE TABLE IF NOT EXISTS $DatabaseTable"
                        + "("
                        + "$ColumnId TEXT PRIMARY KEY,"
                        + "$ColumnDateTime INTEGER,"
                        + "$ColumnSeverity  INTEGER,"
                        + "$ColumnTag  TEXT,"
                        + "$ColumnDescription  TEXT"
                        + ")")
        database.execSQL(createTable)
    }

    override fun onDowngrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) = onUpgrade(database, oldVersion, newVersion)

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        database.execSQL("DROP TABLE IF EXISTS $DatabaseTable")
        onCreate(database)
    }

    fun addLog(dbLog: DbLog): Long {
        val values = ContentValues().apply {
            put(ColumnId, dbLog.id)
            put(ColumnDateTime, dbLog.dateTime.toString())
            put(ColumnSeverity, dbLog.severity.ordinal.toString())
            put(ColumnTag, dbLog.tag)
            put(ColumnDescription, dbLog.description)
        }

        return this.writableDatabase.insert(DatabaseTable, null, values)
    }

    companion object {
        private const val DatabaseVersion = 1
        private const val DatabaseName = "guepardoapps-stopme-logging-2.db"
        private const val DatabaseTable = "loggingTable"

        private const val ColumnId = "id"
        private const val ColumnDateTime = "dateTime"
        private const val ColumnSeverity = "severity"
        private const val ColumnTag = "tag"
        private const val ColumnDescription = "description"
    }
}