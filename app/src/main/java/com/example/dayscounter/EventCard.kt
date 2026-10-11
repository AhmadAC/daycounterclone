package com.example.dayscounter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EventCard(event: Event, onClick: () -> Unit) {
    val today = LocalDate.now()
    val eventDate = LocalDate.ofEpochDay(event.epochDay)
    val isPassed = !eventDate.isAfter(today)

    // Calculate time using the exact combination of checkboxes selected
    val diff = calculateTimeDiff(
        startDate = if (isPassed) eventDate else today,
        endDate = if (isPassed) today else eventDate,
        event = event
    )

    // Build the dynamic string (e.g. "41" for Days, or "1/10" for Months/Days)
    val values = mutableListOf<String>()
    val labels = mutableListOf<String>()

    if (event.showYears) { values.add(diff.years.toString()); labels.add("years") }
    if (event.showMonths) { values.add(diff.months.toString()); labels.add("months") }
    if (event.showWeeks) { values.add(diff.weeks.toString()); labels.add("weeks") }
    if (event.showDays) { values.add(diff.days.toString()); labels.add("days") }

    if (values.isEmpty()) {
        values.add("0")
        labels.add("days")
    }

    val mainText = values.joinToString("/")
    val subText = labels.joinToString("/")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF282828)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(event.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Normal)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        eventDate.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isPassed) "Passed" else "Left",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Text(
                        text = mainText,
                        color = Color(event.colorHex),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(subText, color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}
