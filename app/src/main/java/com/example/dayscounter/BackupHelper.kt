package com.example.dayscounter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.gson.*
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object BackupHelper {
    // 1. Local Export
    fun writeBackup(context: Context, uri: Uri, events: List<Event>): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val json = GsonBuilder().setPrettyPrinting().create().toJson(events)
                outputStream.write(json.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 2. Resilient Local Import (Strips WeChat BOM, accepts multiple date formats and JSON structures)
    fun readBackup(context: Context, uri: Uri): Result<List<Event>> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Cannot open file stream"))
            val rawJson = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            
            // Remove UTF-8 Byte Order Mark (\uFEFF) injected by WeChat and file managers
            val cleanJson = rawJson.trim().removePrefix("\uFEFF")
            
            // Force Dynamic JSON parsing to ensure Kotlin default values are respected
            // (Gson direct-cast bypasses Kotlin null-safety on missing fields)
            val jsonElement = JsonParser.parseString(cleanJson)
            val jsonArray = when {
                jsonElement.isJsonArray -> jsonElement.asJsonArray
                jsonElement.isJsonObject -> {
                    val obj = jsonElement.asJsonObject
                    when {
                        obj.has("events") && obj.get("events").isJsonArray -> obj.getAsJsonArray("events")
                        obj.has("data") && obj.get("data").isJsonArray -> obj.getAsJsonArray("data")
                        obj.has("items") && obj.get("items").isJsonArray -> obj.getAsJsonArray("items")
                        else -> JsonArray().apply { add(obj) }
                    }
                }
                else -> return Result.failure(Exception("Unsupported JSON structure"))
            }

            val parsedEvents = mutableListOf<Event>()
            val now = LocalDate.now()

            for (element in jsonArray) {
                if (!element.isJsonObject) continue
                val obj = element.asJsonObject

                val title = obj.getStr("title")
                    ?: obj.getStr("name")
                    ?: obj.getStr("eventTitle")
                    ?: obj.getStr("text")
                    ?: "Untitled"

                val description = obj.getStr("description")
                    ?: obj.getStr("desc")
                    ?: obj.getStr("note")
                    ?: ""

                // Default safely to "Normal" if the field is missing in older backups
                val label = obj.getStr("label") ?: "Normal"

                val epochDay = parseEpochDay(obj, now)
                val colorHex = parseColor(obj)

                val showYears = obj.getBool("showYears") ?: false
                val showMonths = obj.getBool("showMonths") ?: false
                val showWeeks = obj.getBool("showWeeks") ?: false
                val showDays = obj.getBool("showDays") ?: true

                parsedEvents.add(
                    Event(
                        id = 0, // Reset ID so Room inserts them as new distinct events
                        title = title,
                        description = description,
                        label = label,
                        epochDay = epochDay,
                        createdAt = obj.getLongOrNull("createdAt") ?: System.currentTimeMillis(),
                        colorHex = colorHex,
                        showYears = showYears,
                        showMonths = showMonths,
                        showWeeks = showWeeks,
                        showDays = showDays
                    )
                )
            }

            if (parsedEvents.isEmpty()) {
                Result.failure(Exception("No events found in file"))
            } else {
                Result.success(parsedEvents)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun JsonObject.getStr(key: String): String? =
        if (has(key) && !get(key).isJsonNull) get(key).asString else null

    private fun JsonObject.getLongOrNull(key: String): Long? =
        if (has(key) && !get(key).isJsonNull) runCatching { get(key).asLong }.getOrNull() else null

    private fun JsonObject.getBool(key: String): Boolean? =
        if (has(key) && !get(key).isJsonNull) runCatching { get(key).asBoolean }.getOrNull() else null

    private fun parseEpochDay(obj: JsonObject, defaultDate: LocalDate): Long {
        obj.getLongOrNull("epochDay")?.let { return it }

        val rawNum = obj.getLongOrNull("date")
            ?: obj.getLongOrNull("targetDate")
            ?: obj.getLongOrNull("timestamp")
            ?: obj.getLongOrNull("time")
        if (rawNum != null) {
            return when {
                rawNum > 100_000_000_000L ->
                    Instant.ofEpochMilli(rawNum).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                rawNum > 1_000_000_000L ->
                    Instant.ofEpochSecond(rawNum).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                else -> rawNum
            }
        }

        val rawDateStr = obj.getStr("date") ?: obj.getStr("targetDate") ?: obj.getStr("eventDate")
        if (!rawDateStr.isNullOrBlank()) {
            val patterns = listOf(
                "yyyy-MM-dd", "d MMM yyyy", "dd MMM yyyy", "d/M/yyyy", "dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd"
            )
            for (pattern in patterns) {
                try {
                    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                    return LocalDate.parse(rawDateStr.trim(), formatter).toEpochDay()
                } catch (_: Exception) { }
            }
        }

        return defaultDate.toEpochDay()
    }

    private fun parseColor(obj: JsonObject): Long {
        obj.getLongOrNull("colorHex")?.let { return it }
        val rawColor = obj.get("color")
        if (rawColor != null && !rawColor.isJsonNull) {
            runCatching { return rawColor.asLong }
            val str = rawColor.asString
            if (str.startsWith("#")) {
                runCatching { return android.graphics.Color.parseColor(str).toLong() and 0xFFFFFFFFL }
            }
        }
        return 0xFFF16529
    }

    // 3. Send Backup to WeChat
    fun sendBackupToWeChat(context: Context, events: List<Event>) {
        try {
            val json = GsonBuilder().setPrettyPrinting().create().toJson(events)
            val fileName = "days_counter_backup_${LocalDate.now()}.json"
            val backupFile = File(context.cacheDir, fileName)
            backupFile.writeText(json, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                backupFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.tencent.mm")
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                val chooser = Intent.createChooser(intent.apply { `package` = null }, "Share Backup via")
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}