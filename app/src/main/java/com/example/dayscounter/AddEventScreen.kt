package com.example.dayscounter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewEventScreen(
    event: Event,
    isDarkTheme: Boolean = true,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (Event) -> Unit
) {
    var showColorPicker by remember { mutableStateOf(false) }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = event.colorHex,
            onColorSelected = { newColor ->
                onUpdate(event.copy(colorHex = newColor))
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    val bgColor = if (isDarkTheme) Color(0xFF1B1B1B) else Color(0xFFF2F2F7)
    val cardColor = if (isDarkTheme) Color(0xFF333333) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black

    Scaffold(
        containerColor = bgColor,
        modifier = Modifier.fillMaxSize().swipeToBack(onBack), // Swipe left or right to go back
        topBar = {
            TopAppBar(
                title = { Text("View event", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = textColor)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = textColor)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EventCard(event = event, isDarkTheme = isDarkTheme, onClick = {})

            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (event.description.isBlank()) "No description" else event.description,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 16.sp
                )
            }

            // COLOR PALETTE BLOCK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PaletteColors.forEach { colorVal ->
                            val isSelected = event.colorHex == colorVal
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(colorVal))
                                    .clickable { onUpdate(event.copy(colorHex = colorVal)) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).clickable { showColorPicker = true },
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(event.colorHex))
                        )
                        Icon(Icons.Default.Palette, contentDescription = "Custom Color", tint = textColor)
                    }
                }
            }

            // CHECKBOXES BLOCK
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val today = LocalDate.now()
                val eventDate = LocalDate.ofEpochDay(event.epochDay)
                val isPassed = !eventDate.isAfter(today)
                val diff = calculateTimeDiff(if(isPassed) eventDate else today, if(isPassed) today else eventDate, event)

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            TimeUnitCheckbox("Years", event.showYears, diff.years, event.colorHex, textColor) { onUpdate(event.copy(showYears = it)) }
                            TimeUnitCheckbox("Months", event.showMonths, diff.months, event.colorHex, textColor) { onUpdate(event.copy(showMonths = it)) }
                        }
                        Column(Modifier.weight(1f)) {
                            TimeUnitCheckbox("Weeks", event.showWeeks, diff.weeks, event.colorHex, textColor) { onUpdate(event.copy(showWeeks = it)) }
                            TimeUnitCheckbox("Days", event.showDays, diff.days, event.colorHex, textColor) { onUpdate(event.copy(showDays = it)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeUnitCheckbox(
    label: String,
    isChecked: Boolean,
    value: Long,
    colorHex: Long,
    textColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    val color = Color(colorHex)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = color,
                uncheckedColor = Color(0xFFF16529),
                checkmarkColor = Color.White
            )
        )
        Text(text = "$label: ", color = textColor, fontSize = 16.sp)
        Text(
            text = if (isChecked) value.toString() else "-",
            color = color,
            fontSize = 18.sp
        )
    }
}

@Composable
fun ColorPickerDialog(
    initialColor: Long,
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var red by remember { mutableFloatStateOf(Color(initialColor).red) }
    var green by remember { mutableFloatStateOf(Color(initialColor).green) }
    var blue by remember { mutableFloatStateOf(Color(initialColor).blue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2C2C2C),
        title = { Text("Select Custom Color", color = Color.White) },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(red, green, blue))
                )
                Spacer(Modifier.height(16.dp))
                
                Text("Red", color = Color.White)
                Slider(value = red, onValueChange = { red = it }, colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red))
                
                Text("Green", color = Color.White)
                Slider(value = green, onValueChange = { green = it }, colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green))
                
                Text("Blue", color = Color.White)
                Slider(value = blue, onValueChange = { blue = it }, colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newColor = Color(red, green, blue)
                val colorLong = (0xFFL shl 24) or
                        ((newColor.red * 255).toLong() shl 16) or
                        ((newColor.green * 255).toLong() shl 8) or
                        ((newColor.blue * 255).toLong())
                onColorSelected(colorLong)
            }) { Text("SAVE", color = Color(0xFFF16529)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.Gray) }
        }
    )
}