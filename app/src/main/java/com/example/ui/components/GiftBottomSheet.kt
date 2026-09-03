package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.data.MicSeat
import com.example.data.VirtualGift
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftBottomSheet(
    gifts: List<VirtualGift>,
    userCoins: Long,
    receiverSeat: MicSeat?,
    seats: List<MicSeat>,
    onSendGift: (VirtualGift, Int) -> Unit,
    onDismiss: () -> Unit,
    onRechargeClick: () -> Unit
) {
    var selectedGift by remember { mutableStateOf(gifts.firstOrNull()) }
    var selectedMultiplier by remember { mutableIntStateOf(1) }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Popular", "Luxury", "Effects", "Special")
    val multipliers = listOf(1, 5, 10, 66, 99)

    val filteredGifts = remember(selectedCategory) {
        if (selectedCategory == "All") gifts
        else gifts.filter { it.category == selectedCategory }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = VibrantSurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = VibrantBorder)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Row: Recipient info & close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Send To: ",
                        color = VibrantTextSecondary,
                        fontSize = 13.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(VibrantSurfaceVariant)
                            .border(1.dp, VibrantBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val name = receiverSeat?.user?.name ?: "All on Stage 🎙️"
                        Text(
                            text = name,
                            color = VibrantGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // User Coin balance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(VibrantSurfaceVariant)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(16.dp))
                        .clickable { onRechargeClick() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(text = "🪙 ", fontSize = 12.sp)
                    Text(
                        text = "$userCoins",
                        color = VibrantGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Recharge",
                        tint = VibrantPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) VibrantPrimary else VibrantSurfaceVariant
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else VibrantBorder,
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else VibrantTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gifts Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredGifts) { gift ->
                    val isSelected = selectedGift?.id == gift.id
                    Card(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) VibrantPrimary else VibrantBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedGift = gift },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) VibrantPrimaryContainer else VibrantSurfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = gift.icon, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = gift.name,
                                color = VibrantTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🪙", fontSize = 9.sp)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${gift.costCoins}",
                                    color = VibrantGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multiplier Bar & Send Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Multipliers
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    multipliers.forEach { count ->
                        val isMulti = selectedMultiplier == count
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isMulti) VibrantPrimary else VibrantSurfaceVariant
                                )
                                .border(
                                    1.dp,
                                    if (isMulti) Color.Transparent else VibrantBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedMultiplier = count }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "x$count",
                                color = if (isMulti) Color.White else VibrantTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Send Button
                Button(
                    onClick = {
                        selectedGift?.let { onSendGift(it, selectedMultiplier) }
                    },
                    enabled = selectedGift != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    val totalCost = (selectedGift?.costCoins ?: 0) * selectedMultiplier
                    Text(
                        text = "Send ($totalCost 🪙)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
