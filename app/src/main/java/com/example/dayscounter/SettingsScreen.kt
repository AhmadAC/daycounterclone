package com.example.dayscounter

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    events: List<Event>,
    isDarkTheme: Boolean,
    useSystemTheme: Boolean,
    effectiveDark: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleSystemTheme: (Boolean) -> Unit,
    onBack: () -> Unit,
    onImportEvents: (List<Event>) -> Unit
) {
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val success = BackupHelper.writeBackup(context, uri, events)
            val msg = if (success) "Backup saved successfully" else "Failed to save backup"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val result = BackupHelper.readBackup(context, uri)
            result.onSuccess { imported ->
                onImportEvents(imported)
                Toast.makeText(context, "Restored ${imported.size} events", Toast.LENGTH_SHORT).show()
            }.onFailure { err ->
                Toast.makeText(context, "Import failed: ${err.localizedMessage ?: "Invalid file"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val bgColor = if (effectiveDark) Color(0xFF1B1B1B) else Color(0xFFF2F2F7)
    val textColor = if (effectiveDark) Color.White else Color.Black
    val dividerColor = if (effectiveDark) Color(0xFF333333) else Color(0xFFE0E0E0)

    Scaffold(
        containerColor = bgColor,
        modifier = Modifier.fillMaxSize().swipeToBack(onBack), // Swipe left or right to go back
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Backup & Restore Section
            SettingsCategory("Backup & Restore")
            SettingsItem(title = "Upload all events to a file", isDark = effectiveDark, onClick = {
                exportLauncher.launch("days_counter_backup_${LocalDate.now()}.json")
            })
            SettingsItem(title = "Download events from file", isDark = effectiveDark, onClick = {
                importLauncher.launch("*/*")
            })
            SettingsItem(title = "Share backup to WeChat", isDark = effectiveDark, onClick = {
                BackupHelper.sendBackupToWeChat(context, events)
            })

            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            // Interface Settings Section
            SettingsCategory("Interface settings")
            var useRoundButton by remember { mutableStateOf(true) }
            SettingsSwitchItem(title = "Use the primary round button", isChecked = useRoundButton, isDark = effectiveDark) {
                useRoundButton = it
            }
            
            SettingsItem(title = "Date display format", subtitle = "1 Jan 1970", isDark = effectiveDark, onClick = { })
            
            // Dark Theme Switch: enabled and working when System Theme is unticked
            SettingsSwitchItem(
                title = "Dark Theme",
                isChecked = if (useSystemTheme) effectiveDark else isDarkTheme,
                enabled = !useSystemTheme,
                isDark = effectiveDark,
                onCheckedChange = onToggleDarkTheme
            )
            
            SettingsCheckboxItem(
                title = "Use system theme",
                isChecked = useSystemTheme,
                isDark = effectiveDark,
                onCheckedChange = onToggleSystemTheme
            )

            HorizontalDivider(color = dividerColor, thickness = 1.dp)

            // About Section (Support section & Privacy/Contact removed per specification)
            SettingsCategory("About")
            SettingsItem(title = "Developer", subtitle = "A. A. Cooper", isDark = effectiveDark, onClick = { })
            SettingsItem(title = "Version", subtitle = "1.0.0", isDark = effectiveDark, onClick = { })
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        color = Color(0xFFF16529),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(title: String, subtitle: String? = null, isDark: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(text = title, color = if (isDark) Color.White else Color.Black, fontSize = 16.sp)
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    isChecked: Boolean,
    enabled: Boolean = true,
    isDark: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = { onCheckedChange(!isChecked) })
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (!enabled) Color.Gray else if (isDark) Color.White else Color.Black,
            fontSize = 16.sp
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFF16529)
            )
        )
    }
}

@Composable
fun SettingsCheckboxItem(
    title: String,
    isChecked: Boolean,
    isDark: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onCheckedChange(!isChecked) })
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = if (isDark) Color.White else Color.Black, fontSize = 16.sp)
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFFF16529),
                checkmarkColor = Color.White
            )
        )
    }
}