package guepardoapps.stopme.logging

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context

// Helpful
// https://developer.android.com/training/data-storage/sqlite
// https://www.techotopia.com/index.php/A_Kotlin_Android_SQLite_Database_Tutorial

internal class DbLogging(context: Context)
    : SQLiteOpenHelper(context, DatabaseName, null, DatabaseVersion) {

    override fun onCreate(database: SQLiteDatabase) {
        val createTable = (
                "CREATE TABLE IF NOT EXISTS $DatabaseTable"
                        + "("
                        + "$ColumnId INTEGER PRIMARY KEY autoincrement,"
                        + "$ColumnDateTime INTEGER,"
                        + "$ColumnSeverity  INTEGER,"
                        + "$ColumnTag  TEXT,"
                        + "$ColumnDescription  TEXT"
                        + ")")
        database.execSQL(createTable)
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        database.execSQL("DROP TABLE IF EXISTS $DatabaseTable")
        onCreate(database)
    }

    override fun onDowngrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(database, oldVersion, newVersion)
    }

    fun addLog(dbLog: DbLog): Long {
        val values = ContentValues().apply {
            put(ColumnDateTime, dbLog.dateTime.toString())
            put(ColumnSeverity, dbLog.severity.ordinal.toString())
            put(ColumnTag, dbLog.tag)
            put(ColumnDescription, dbLog.description)
        }

        val database = this.writableDatabase
        return database.insert(DatabaseTable, null, values)
    }

    companion object {
        private const val DatabaseVersion = 2
        private const val DatabaseName = "guepardoapps-stopme-logging.db"
        private const val DatabaseTable = "loggingTable"

        private const val ColumnId = "_id"
        private const val ColumnDateTime = "dateTime"
        private const val ColumnSeverity = "severity"
        private const val ColumnTag = "tag"
        private const val ColumnDescription = "description"
    }
}