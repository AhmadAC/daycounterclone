package com.example.dayscounter

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.gson.Gson
import java.io.File
import java.time.LocalDate

object WeChatBackupHelper {
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
                setPackage("com.tencent.mm") // Explicitly targets WeChat
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // If WeChat is not installed, open standard share sheet
                val chooser = Intent.createChooser(intent.apply { `package` = null }, "Share Backup via")
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
