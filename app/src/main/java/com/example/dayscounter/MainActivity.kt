package com.example.dayscounter

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Palette colors matching the screenshot
val PaletteColors = listOf(
    0xFFF16529, 0xFFE53935, 0xFF8E24AA, 0xFF1E88E5, 0xFF43A047, 0xFFFDD835
)

enum class SortOption {
    TITLE_AZ, TITLE_ZA, DATE_ASC, DATE_DESC, ADDED_ASC, ADDED_DESC
}

class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "days_counter.db").build()
    }

    override fun病毒 onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            val events by db.eventDao().getAllEvents().collectAsState(initial = emptyList())
            
            var currentScreen by remember { mutableStateOf("HOME") }
            var selectedEvent by remember { mutableStateOf<Event?>(null) }

            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1B1B1B)) {
                when (currentScreen) {
                    "HOME" -> HomeScreen(
                        events = events,
                        onAddClick = { currentScreen = "ADD" },
                        onEventClick = { event ->
                            selectedEvent = event
                            currentScreen = "VIEW"
                        },
                        onBackupToWeChat = { WeChatBackupHelper.sendBackupToWeChat(this, events) }
                    )
                    "ADD" -> AddEventScreen(
                        onBack = { currentScreen = "HOME" },
                        onSave = { newEvent ->
                            scope.launch { db.eventDao().insertOrUpdate(newEvent) }
                            currentScreen = "HOME"
                        }
                    )
                    "VIEW" -> selectedEvent?.let { event ->
                        ViewEventScreen(
                            event = event,
                            onBack = { currentScreen = "HOME" },
                            onDelete = {
                                scope.launch { db.eventDao().delete(event) }
                                currentScreen = "HOME"
                            },
                            onUpdate = { updated ->
                                scope.launch { db.eventDao().insertOrUpdate(updated) }
                                selectedEvent = updated
                            }
                        )
                    }
                }
            }
        }
    }
}
