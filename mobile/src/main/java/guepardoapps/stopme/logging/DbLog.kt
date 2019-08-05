package guepardoapps.stopme.logging

import java.sql.Date

internal data class DbLog(val id: String, val dateTime: Date, val severity: Severity, val tag: String, val description: String)