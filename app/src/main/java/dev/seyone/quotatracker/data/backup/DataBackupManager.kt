package dev.seyone.quotatracker.data.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.seyone.quotatracker.data.local.QuotaDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DataBackupManager(
    private val context: Context,
    private val database: QuotaDatabase
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportBackup(destinationUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val quotas = database.quotaDao().getAllQuotas().firstOrNull() ?: emptyList()
            val logEntries = database.logEntryDao().getAllLogEntries().firstOrNull() ?: emptyList()
            val snapshots = database.weeklySnapshotDao().getAllSnapshots().firstOrNull() ?: emptyList()

            val backupData = QuotaBackupData(
                quotas = quotas,
                logEntries = logEntries,
                weeklySnapshots = snapshots
            )

            val jsonString = gson.toJson(backupData)

            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                }
            } ?: return@withContext Result.failure(Exception("Unable to open output stream"))

            Log.d("DataBackupManager", "Successfully exported JSON backup to $destinationUri")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DataBackupManager", "Error exporting JSON backup", e)
            Result.failure(e)
        }
    }

    suspend fun importBackup(sourceUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext Result.failure(Exception("Unable to open input stream"))

            val backupData = gson.fromJson(jsonString, QuotaBackupData::class.java)
                ?: return@withContext Result.failure(Exception("Invalid or empty backup file"))

            database.withTransaction {
                database.clearAllTables()

                backupData.quotas.forEach { quota ->
                    database.quotaDao().insertQuota(quota)
                }
                if (backupData.logEntries.isNotEmpty()) {
                    database.logEntryDao().insertLogs(backupData.logEntries)
                }
                if (backupData.weeklySnapshots.isNotEmpty()) {
                    backupData.weeklySnapshots.forEach { snapshot ->
                        database.weeklySnapshotDao().insertSnapshot(snapshot)
                    }
                }
            }

            Log.d("DataBackupManager", "Successfully imported backup from $sourceUri")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DataBackupManager", "Error importing backup", e)
            Result.failure(e)
        }
    }

    suspend fun exportCsv(destinationUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val logsWithQuotas = database.logEntryDao().getAllLogsWithQuotas().firstOrNull() ?: emptyList()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val zoneId = ZoneId.systemDefault()

            val csvContent = StringBuilder().apply {
                append("Log ID,Timestamp,Date Time,Quota Title,Duration Minutes\n")
                logsWithQuotas.forEach { item ->
                    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(item.logEntry.timestamp), zoneId)
                    val dateStr = dateTime.format(formatter)
                    val title = (item.quota?.title ?: "Deleted Quota").replace(",", " ")
                    append("${item.logEntry.id},${item.logEntry.timestamp},\"$dateStr\",\"$title\",${item.logEntry.durationMinutes}\n")
                }
            }.toString()

            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(csvContent)
                }
            } ?: return@withContext Result.failure(Exception("Unable to open output stream"))

            Log.d("DataBackupManager", "Successfully exported CSV to $destinationUri")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DataBackupManager", "Error exporting CSV", e)
            Result.failure(e)
        }
    }

    suspend fun generateCsvAnalyticsReport(destinationUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val snapshotsWithQuotas = database.weeklySnapshotDao().getAllSnapshotsWithQuotas().firstOrNull() ?: emptyList()
            val logsWithQuotas = database.logEntryDao().getAllLogsWithQuotas().firstOrNull() ?: emptyList()

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val zoneId = ZoneId.systemDefault()

            val csvContent = StringBuilder().apply {
                append("Date,QuotaTitle,TargetMinutes,LoggedMinutes,Type\n")

                // Snapshots
                snapshotsWithQuotas.forEach { item ->
                    val title = (item.quota?.title ?: "Quota #${item.snapshot.quotaId}").replace(",", " ")
                    append("${item.snapshot.weekString},\"$title\",${item.snapshot.targetMinutesSnapshot},${item.snapshot.loggedMinutesSnapshot},Snapshot\n")
                }

                // Log Entries
                logsWithQuotas.forEach { item ->
                    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(item.logEntry.timestamp), zoneId)
                    val dateStr = dateTime.format(formatter)
                    val title = (item.quota?.title ?: "Deleted Quota").replace(",", " ")
                    val target = item.quota?.targetMinutes ?: 0
                    append("\"$dateStr\",\"$title\",$target,${item.logEntry.durationMinutes},Log\n")
                }
            }.toString()

            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(csvContent)
                }
            } ?: return@withContext Result.failure(Exception("Unable to open output stream"))

            Log.d("DataBackupManager", "Successfully exported CSV Analytics Report to $destinationUri")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DataBackupManager", "Error generating CSV Analytics Report", e)
            Result.failure(e)
        }
    }
}
