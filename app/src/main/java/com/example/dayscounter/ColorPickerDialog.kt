package com.example.dayscounter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
