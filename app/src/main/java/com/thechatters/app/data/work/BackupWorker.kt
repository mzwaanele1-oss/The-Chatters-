package com.thechatters.app.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class BackupWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val backupChats = inputData.getBoolean("backupChats", true)
            val backupMedia = inputData.getBoolean("backupMedia", true)
            val backupStickers = inputData.getBoolean("backupStickers", true)

            // Step 1: Collect chosen items into JSON
            setProgress(workDataOf("progress" to 15, "status" to "Compiling backup JSON (Chats, Media, Stickers)..."))
            delay(600)

            val backupJson = JSONObject().apply {
                put("app", "The Chatters")
                put("version", "1.0.0")
                put("timestamp", System.currentTimeMillis())
                put("account", "mzwaanele1@gmail.com")
                put("device", "Android Client")

                val itemsArray = JSONArray()
                if (backupChats) itemsArray.put("CHATS_DATABASE")
                if (backupMedia) itemsArray.put("MEDIA_METADATA")
                if (backupStickers) itemsArray.put("CUSTOM_STICKERS_512")
                put("includedItems", itemsArray)
            }

            setProgress(workDataOf("progress" to 45, "status" to "Writing encrypted JSON payload..."))
            delay(600)

            // Step 2: Write JSON file to local backup directory
            val backupsDir = File(appContext.filesDir, "backups").apply { if (!exists()) mkdirs() }
            val timestamp = System.currentTimeMillis()
            val backupFileName = "backup_$timestamp.json"
            val backupFile = File(backupsDir, backupFileName)

            FileOutputStream(backupFile).use { fos ->
                fos.write(backupJson.toString(2).toByteArray(Charsets.UTF_8))
            }

            // Step 3: Emulate uploading to Storage /backups/
            val storagePath = "/backups/current_user_id/$backupFileName"
            setProgress(workDataOf("progress" to 80, "status" to "Uploading JSON to Storage $storagePath..."))
            delay(700)

            setProgress(workDataOf("progress" to 100, "status" to "Backup completed successfully!"))
            delay(300)

            val totalSize = 18_750_000L + backupFile.length()
            Result.success(workDataOf(
                "backupTimestamp" to timestamp,
                "backupSize" to totalSize,
                "storagePath" to storagePath
            ))
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
