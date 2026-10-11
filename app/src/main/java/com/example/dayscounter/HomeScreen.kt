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

    Scaffold(
        containerColor = Color(0xFF1A1A1A),
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF333333),
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
                    Text("Days Counter", fontSize = 22.sp, color = Color.White)
                    Row {
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Sort", tint = Color.LightGray)
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.LightGray)
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

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val alignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterLeft else Alignment.CenterRight
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
                    EventCard(event = event, onClick = { onEventClick(event) })
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