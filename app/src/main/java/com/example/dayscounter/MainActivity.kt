package com.example.dayscounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.room.Room
import kotlinx.coroutines.launch
import kotlin.math.abs

val PaletteColors = listOf(
    0xFFF16529, 0xFFE53935, 0xFF8E24AA, 0xFF1E88E5, 0xFF43A047, 0xFFFDD835
)

enum class SortOption {
    TITLE_AZ, TITLE_ZA, DATE_ASC, DATE_DESC, ADDED_ASC, ADDED_DESC
}

// Swipe left or right to go back to the previous screen
fun Modifier.swipeToBack(onBack: () -> Unit): Modifier = this.pointerInput(Unit) {
    var totalDrag = 0f
    detectHorizontalDragGestures(
        onDragStart = { totalDrag = 0f },
        onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
        onDragEnd = {
            if (abs(totalDrag) > 100f) {
                onBack()
            }
        }
    )
}

class MainActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "days_counter.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            val events by db.eventDao().getAllEvents().collectAsState(initial = emptyList())
            
            var currentScreen by remember { mutableStateOf("HOME") }
            var selectedEvent by remember { mutableStateOf<Event?>(null) }

            // Theme Management: system theme toggle + dark mode toggle
            var useSystemTheme by remember { mutableStateOf(true) }
            var isDarkTheme by remember { mutableStateOf(true) }
            val systemDark = isSystemInDarkTheme()
            val effectiveDark = if (useSystemTheme) systemDark else isDarkTheme

            val appBgColor = if (effectiveDark) Color(0xFF1B1B1B) else Color(0xFFF2F2F7)

            MaterialTheme(
                colorScheme = if (effectiveDark) darkColorScheme() else lightColorScheme()
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = appBgColor) {
                    when (currentScreen) {
                        "HOME" -> HomeScreen(
                            events = events,
                            isDarkTheme = effectiveDark,
                            onAddClick = { currentScreen = "ADD" },
                            onEventClick = { event ->
                                selectedEvent = event
                                currentScreen = "VIEW"
                            },
                            onSettingsClick = { currentScreen = "SETTINGS" },
                            onDeleteEvent = { event ->
                                scope.launch { db.eventDao().delete(event) }
                            },
                            onRestoreEvent = { event ->
                                scope.launch { db.eventDao().insertOrUpdate(event) }
                            }
                        )
                        "ADD" -> AddEventScreen(
                            isDarkTheme = effectiveDark,
                            onBack = { currentScreen = "HOME" },
                            onSave = { newEvent ->
                                scope.launch { db.eventDao().insertOrUpdate(newEvent) }
                                currentScreen = "HOME"
                            }
                        )
                        "VIEW" -> selectedEvent?.let { event ->
                            ViewEventScreen(
                                event = event,
                                isDarkTheme = effectiveDark,
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
                        "SETTINGS" -> SettingsScreen(
                            events = events,
                            isDarkTheme = isDarkTheme,
                            useSystemTheme = useSystemTheme,
                            effectiveDark = effectiveDark,
                            onToggleDarkTheme = { isDarkTheme = it },
                            onToggleSystemTheme = { useSystemTheme = it },
                            onBack = { currentScreen = "HOME" },
                            onImportEvents = { importedEvents ->
                                scope.launch {
                                    importedEvents.forEach { db.eventDao().insertOrUpdate(it.copy(id = 0)) }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}