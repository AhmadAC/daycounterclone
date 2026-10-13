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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

    var selectedLabel by remember { mutableStateOf("Normal") }

    val allLabels = remember(events) {
        val labels = events.map { it.label.ifBlank { "Normal" } }.toMutableSet()
        labels.add("Normal")
        labels.sorted()
    }

    if (selectedLabel !in allLabels) {
        selectedLabel = "Normal"
    }

    val filteredEvents = remember(events, selectedLabel) {
        events.filter { (it.label.ifBlank { "Normal" }) == selectedLabel }
    }

    val sortedEvents = remember(filteredEvents, currentSort) {
        when (currentSort) {
            SortOption.TITLE_AZ -> filteredEvents.sortedBy { it.title.lowercase() }
            SortOption.TITLE_ZA -> filteredEvents.sortedByDescending { it.title.lowercase() }
            SortOption.DATE_ASC -> filteredEvents.sortedBy { it.epochDay }
            SortOption.DATE_DESC -> filteredEvents.sortedByDescending { it.epochDay }
            SortOption.ADDED_ASC -> filteredEvents.sortedBy { it.createdAt }
            SortOption.ADDED_DESC -> filteredEvents.sortedByDescending { it.createdAt }
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
            Column(modifier = Modifier.background(containerBg)) {
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
                ScrollableTabRow(
                    selectedTabIndex = allLabels.indexOf(selectedLabel).coerceAtLeast(0),
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFF16529),
                    edgePadding = 16.dp,
                    divider = { HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f)) },
                    indicator = { tabPositions ->
                        val safeIndex = allLabels.indexOf(selectedLabel).coerceIn(0, tabPositions.size - 1)
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[safeIndex]),
                            color = Color(0xFFF16529)
                        )
                    }
                ) {
                    allLabels.forEach { label ->
                        Tab(
                            selected = selectedLabel == label,
                            onClick = { selectedLabel = label },
                            text = {
                                Text(
                                    text = label,
                                    color = if (selectedLabel == label) Color(0xFFF16529) else Color.Gray
                                )
                            }
                        )
                    }
                }
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
            item { Spacer(modifier = Modifier.height(4.dp)) }
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
            item { Spacer(modifier = Modifier.height(60.dp)) }
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