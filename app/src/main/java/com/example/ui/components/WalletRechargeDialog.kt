package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

data class CoinPackage(val coins: Long, val bonus: Long, val price: String)

@Composable
fun WalletRechargeDialog(
    currentCoins: Long,
    currentDiamonds: Long,
    onRecharge: (Long) -> Unit,
    onExchangeDiamonds: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val packages = listOf(
        CoinPackage(1000, 0, "$0.99"),
        CoinPackage(5500, 500, "$4.99"),
        CoinPackage(12000, 2000, "$9.99"),
        CoinPackage(30000, 5000, "$24.99"),
        CoinPackage(70000, 15000, "$49.99"),
        CoinPackage(150000, 40000, "$99.99")
    )

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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🪙", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Coin Recharge & Vault",
                            color = VibrantTextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VibrantTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Balances Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(VibrantSurfaceVariant)
                        .border(1.dp, VibrantBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Gold Coins", color = VibrantTextSecondary, fontSize = 11.sp)
                        Text(text = "🪙 $currentCoins", color = VibrantGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Diamonds", color = VibrantTextSecondary, fontSize = 11.sp)
                        Text(text = "💎 $currentDiamonds", color = VibrantCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Coin Pack",
                    color = VibrantTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(packages) { pack ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = VibrantSurfaceVariant),
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, VibrantBorder, RoundedCornerShape(14.dp))
                                .clickable { onRecharge(pack.coins + pack.bonus) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🪙 ${pack.coins}",
                                    color = VibrantGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (pack.bonus > 0) {
                                    Text(
                                        text = "+${pack.bonus} Bonus",
                                        color = VibrantPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VibrantPrimary)
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = pack.price,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Exchange Diamonds Button
                if (currentDiamonds >= 1000) {
                    OutlinedButton(
                        onClick = { onExchangeDiamonds(1000) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, VibrantCyan),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Exchange 1,000 💎 for 2,000 🪙", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
