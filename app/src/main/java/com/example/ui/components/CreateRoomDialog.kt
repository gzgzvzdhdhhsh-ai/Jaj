package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.RoomCategory
import com.example.ui.theme.*

@Composable
fun CreateRoomDialog(
    onCreateRoom: (title: String, category: RoomCategory, tag: String, wallpaper: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("My Voice Party & Adda") }
    var selectedCategory by remember { mutableStateOf(RoomCategory.POPULAR) }
    var selectedTag by remember { mutableStateOf("Music") }

    val tags = listOf("Music", "Ludo", "Chat", "Bangla", "Friendship", "Singing")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = VibrantSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, VibrantBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Voice Room 🎙️",
                        color = VibrantTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VibrantTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Room Name",
                    color = VibrantTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = VibrantTextPrimary,
                        unfocusedTextColor = VibrantTextPrimary,
                        focusedBorderColor = VibrantPrimary,
                        unfocusedBorderColor = VibrantBorder,
                        focusedContainerColor = VibrantSurfaceVariant,
                        unfocusedContainerColor = VibrantSurfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Category",
                    color = VibrantTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(RoomCategory.POPULAR, RoomCategory.BANGLA, RoomCategory.MUSIC, RoomCategory.GAMING).forEach { cat ->
                        val isSel = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) VibrantPrimary else VibrantSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSel) Color.Transparent else VibrantBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${cat.icon} ${cat.label.take(7)}",
                                color = if (isSel) Color.White else VibrantTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Topic Tag",
                    color = VibrantTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.take(4).forEach { tag ->
                        val isSel = selectedTag == tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) VibrantPrimary else VibrantSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSel) Color.Transparent else VibrantBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedTag = tag }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                color = if (isSel) Color.White else VibrantTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onCreateRoom(title, selectedCategory, selectedTag, "room_bg_party")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Go Live & Open Room 🚀",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
