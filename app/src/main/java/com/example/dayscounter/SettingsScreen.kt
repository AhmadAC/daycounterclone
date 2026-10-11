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
            val imported = BackupHelper.readBackup(context, uri)
            if (imported != null) {
                onImportEvents(imported)
                Toast.makeText(context, "Restored ${imported.size} events", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to parse backup", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF1B1B1B),
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1B1B))
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
            SettingsItem(title = "Upload all events to a file", onClick = {
                exportLauncher.launch("days_counter_backup_${LocalDate.now()}.json")
            })
            SettingsItem(title = "Download events from file", onClick = {
                importLauncher.launch("*/*")
            })
            // Added WeChat option back in
            SettingsItem(title = "Share backup to WeChat", onClick = {
                BackupHelper.sendBackupToWeChat(context, events)
            })

            Divider(color = Color(0xFF333333), thickness = 1.dp)

            // Interface Settings Section
            SettingsCategory("Interface settings")
            var useRoundButton by remember { mutableStateOf(true) }
            SettingsSwitchItem(title = "Use the primary round button", isChecked = useRoundButton) { useRoundButton = it }
            
            SettingsItem(title = "Date display format", subtitle = "1 Jan 1970", onClick = { })
            
            SettingsSwitchItem(title = "Dark Theme", isChecked = false, enabled = false) { }
            
            var useSystemTheme by remember { mutableStateOf(true) }
            SettingsCheckboxItem(title = "Use system theme", isChecked = useSystemTheme) { useSystemTheme = it }

            Divider(color = Color(0xFF333333), thickness = 1.dp)

            // Support Section
            SettingsCategory("Support")
            SettingsItem(title = "Rate app", onClick = { })
            SettingsItem(
                title = "Donate to project development",
                subtitle = "Your donations help the app to remain without ads\n(you can donate several times)",
                onClick = { }
            )
            SettingsItem(title = "Share app", subtitle = "Tell your friends and friends about the app.", onClick = { })

            Divider(color = Color(0xFF333333), thickness = 1.dp)

            // About Section
            SettingsCategory("About")
            SettingsItem(title = "Privacy policy", onClick = { })
            SettingsItem(title = "Contact with me", onClick = { })
            SettingsItem(title = "Developer", subtitle = "A. A. Cooper", onClick = { })
            SettingsItem(title = "Version", subtitle = "1.0.0", onClick = { })
            
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
fun SettingsItem(title: String, subtitle: String? = null, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(text = title, color = Color.White, fontSize = 16.sp)
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun SettingsSwitchItem(title: String, isChecked: Boolean, enabled: Boolean = true, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = { onCheckedChange(!isChecked) })
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = if (enabled) Color.White else Color.DarkGray, fontSize = 16.sp)
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
fun SettingsCheckboxItem(title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onCheckedChange(!isChecked) })
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White, fontSize = 16.sp)
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