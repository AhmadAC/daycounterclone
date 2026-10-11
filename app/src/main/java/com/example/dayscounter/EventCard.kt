package com.example.dayscounter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    val today = LocalDate.now()
    val eventDate = LocalDate.ofEpochDay(event.epochDay)
    val isPassed = !eventDate.isAfter(today)
    val totalDays = ChronoUnit.DAYS.between(
        if (isPassed) eventDate else today,
        if (isPassed) today else eventDate
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF282828)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left color strip
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(Color(event.colorHex))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Title & Date
                Column(modifier = Modifier.weight(1f)) {
                    Text(event.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Normal)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        eventDate.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }

                // Right: Passed/Left + Count
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isPassed) "Passed" else "Left",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    if (event.displayMode == "DAYS") {
                        Text(
                            text = totalDays.toString(),
                            color = Color(event.colorHex),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text("days", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        val period = if (isPassed) Period.between(eventDate, today) else Period.between(today, eventDate)
                        Text(
                            text = "${period.years}/${period.months}/${period.days}",
                            color = Color(event.colorHex),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text("years/months/days", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
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
