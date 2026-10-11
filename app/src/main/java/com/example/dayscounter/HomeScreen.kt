package com.example.dayscounter

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    events: List<Event>,
    isDarkTheme: Boolean = true,
    onAddClick: () -> Unit,
    onEventClick: (Event) -> Unit,
    onSettingsClick: () -> Unit,
    onDeleteEvent: (Event) -> Unit,
    onRestoreEvent: (Event) -> Unit
) {
    var showSortDialog by remember { mutableStateOf(false) }
    var currentSort by remember { mutableStateOf(SortOption.DATE_ASC) }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val sortedEvents = remember(events, currentSort) {
        when (currentSort) {
            SortOption.TITLE_AZ -> events.sortedBy { it.title.lowercase() }
            SortOption.TITLE_ZA -> events.sortedByDescending { it.title.lowercase() }
            SortOption.DATE_ASC -> events.sortedBy { it.epochDay }
            SortOption.DATE_DESC -> events.sortedByDescending { it.epochDay }
            SortOption.ADDED_ASC -> events.sortedBy { it.createdAt }
            SortOption.ADDED_DESC -> events.sortedByDescending { it.createdAt }
        }
    }

    val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    val containerBg = if (isDarkTheme) Color(0xFF1A1A1A) else Color(0xFFF2F2F7)
    val titleColor = if (isDarkTheme) Color.White else Color.Black
    val iconColor = if (isDarkTheme) Color.LightGray else Color.DarkGray

    Scaffold(
        containerColor = containerBg,
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (isDarkTheme) Color(0xFF333333) else Color(0xFF222222),
                    contentColor = Color.White,
                    actionColor = Color(0xFFF16529)
                )
            }
        },
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Days Counter", fontSize = 22.sp, color = titleColor)
                    Row {
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Sort", tint = iconColor)
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = iconColor)
                        }
                    }
                }
                Text(
                    text = "Today: $todayStr",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = Color(0xFFF16529),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sortedEvents, key = { it.id }) { event ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.EndToStart || dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                            onDeleteEvent(event)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val result = snackbarHostState.showSnackbar(
                                    message = "Event deleted",
                                    actionLabel = "RESTORE",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onRestoreEvent(event)
                                }
                            }
                            true
                        } else {
                            false
                        }
                    }
                )

                // Swipe from left-to-right OR right-to-left to delete
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val alignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE53935))
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                ) {
                    EventCard(event = event, isDarkTheme = isDarkTheme, onClick = { onEventClick(event) })
                }
            }
        }
    }

    if (showSortDialog) {
        SortDialog(
            currentSort = currentSort,
            onSelect = { currentSort = it; showSortDialog = false },
            onDismiss = { showSortDialog = false }
        )
    }
}

@Composable
fun SortDialog(currentSort: SortOption, onSelect: (SortOption) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2C2C2C),
        title = { Text("Sort", color = Color.White, fontSize = 20.sp) },
        text = {
            Column {
                val options = listOf(
                    "By title (A-Z)" to SortOption.TITLE_AZ,
                    "By title (Z-A)" to SortOption.TITLE_ZA,
                    "Ascending date" to SortOption.DATE_ASC,
                    "Descending date" to SortOption.DATE_DESC,
                    "Ascending date added" to SortOption.ADDED_ASC,
                    "Descending date added" to SortOption.ADDED_DESC
                )
                options.forEach { (label, option) ->
                    Text(
                        text = label,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color(0xFFF16529))
            }
        }
    )
}