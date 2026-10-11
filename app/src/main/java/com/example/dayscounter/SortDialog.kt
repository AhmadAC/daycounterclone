package com.example.dayscounter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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