package com.example.dayscounter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeToDeleteEventItem(
    event: Event,
    isDarkTheme: Boolean,
    onEventClick: (Event) -> Unit,
    onDeleteEvent: (Event) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val revealWidthPx = with(density) { 88.dp.toPx() }
    val thresholdPx = with(density) { 50.dp.toPx() }

    val offsetX = remember { Animatable(0f) }
    var isRevealed by remember { mutableStateOf(false) }
    var timerJob by remember { mutableStateOf<Job?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        val isSwipedRight = offsetX.value > 0
        val align = if (isSwipedRight) Alignment.CenterStart else Alignment.CenterEnd
        val paddingMod = if (isSwipedRight) Modifier.padding(start = 20.dp) else Modifier.padding(end = 20.dp)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE53935)),
            contentAlignment = align
        ) {
            AnimatedVisibility(
                visible = isRevealed,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = paddingMod
            ) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            offsetX.snapTo(0f)
                            isRevealed = false
                        }
                        onDeleteEvent(event)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(event.id) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            timerJob?.cancel()
                            timerJob = null
                        },
                        onDragEnd = {
                            timerJob?.cancel()
                            timerJob = null
                            coroutineScope.launch {
                                if (isRevealed) {
                                    val target = if (offsetX.value > 0) revealWidthPx else -revealWidthPx
                                    offsetX.animateTo(target, spring(stiffness = Spring.StiffnessMediumLow))
                                } else {
                                    offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                }
                            }
                        },
                        onDragCancel = {
                            timerJob?.cancel()
                            timerJob = null
                            coroutineScope.launch {
                                if (!isRevealed) {
                                    offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (offsetX.value + dragAmount).coerceIn(-revealWidthPx * 1.3f, revealWidthPx * 1.3f)
                            coroutineScope.launch { offsetX.snapTo(newOffset) }

                            if (abs(newOffset) >= thresholdPx) {
                                if (!isRevealed && timerJob == null) {
                                    timerJob = coroutineScope.launch {
                                        delay(1000)
                                        isRevealed = true
                                    }
                                }
                            } else {
                                timerJob?.cancel()
                                timerJob = null
                                if (abs(newOffset) < 10f) {
                                    isRevealed = false
                                }
                            }
                        }
                    )
                }
        ) {
            EventCard(
                event = event,
                isDarkTheme = isDarkTheme,
                onClick = {
                    if (isRevealed || abs(offsetX.value) > 5f) {
                        coroutineScope.launch {
                            offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                            isRevealed = false
                        }
                    } else {
                        onEventClick(event)
                    }
                }
            )
        }
    }
}

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
                SwipeToDeleteEventItem(
                    event = event,
                    isDarkTheme = isDarkTheme,
                    onEventClick = onEventClick,
                    onDeleteEvent = { targetEvent ->
                        onDeleteEvent(targetEvent)
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val result = snackbarHostState.showSnackbar(
                                message = "Event deleted",
                                actionLabel = "RESTORE",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                onRestoreEvent(targetEvent)
                            }
                        }
                    }
                )
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