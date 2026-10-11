package com.example.dayscounter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDate

object BackupHelper {
    // 1. Local Export
    fun writeBackup(context: Context, uri: Uri, events: List<Event>): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val json = Gson().toJson(events)
                outputStream.write(json.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 2. Local Import
    fun readBackup(context: Context, uri: Uri): List<Event>? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader().use { it?.readText() }
            if (json != null) {
                val listType = object : TypeToken<List<Event>>() {}.type
                Gson().fromJson(json, listType)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 3. WeChat Share
    fun sendBackupToWeChat(context: Context, events: List<Event>) {
        try {
            val json = Gson().toJson(events)
            val fileName = "days_counter_backup_${LocalDate.now()}.json"
            val backupFile = File(context.cacheDir, fileName)
            backupFile.writeText(json)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                backupFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.tencent.mm") // Targets WeChat explicitly
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // If WeChat is not installed, fallback to the standard share sheet
                val chooser = Intent.createChooser(intent.apply { `package` = null }, "Share Backup via")
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}