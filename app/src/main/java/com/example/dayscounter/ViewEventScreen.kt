package com.example.dayscounter

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewEventScreen(
    event: Event,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (Event) -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF1B1B1B),
        topBar = {
            TopAppBar(
                title = { Text("View event", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Hook up edit fields if needed */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Event preview card
            EventCard(event = event, onClick = {})

            // Description container
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF282828)),
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

            // Color Palette Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF282828)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PaletteColors.forEach { colorVal ->
                        val isSelected = event.colorHex == colorVal
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(colorVal))
                                .clickable { onUpdate(event.copy(colorHex = colorVal)) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            // Unit display toggle box (Years / Months / Days)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF282828)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = event.displayMode == "YMD",
                                onCheckedChange = { onUpdate(event.copy(displayMode = "YMD")) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(event.colorHex))
                            )
                            Text("Years / Months / Days", color = Color.White)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = event.displayMode == "DAYS",
                                onCheckedChange = { onUpdate(event.copy(displayMode = "DAYS")) },
                                colors = CheckboxDefaults.colors(checkedColor = Color(event.colorHex))
                            )
                            Text("Days only", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
