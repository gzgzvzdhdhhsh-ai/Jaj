package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.NetworkVoiceManager
import com.example.ui.theme.*

@Composable
fun NetworkVoiceDialog(
    networkState: NetworkVoiceManager.NetworkState,
    isUserOnMic: Boolean,
    isMuted: Boolean,
    onDismiss: () -> Unit,
    onAddPeerIp: (String) -> Unit,
    onToggleLoopback: (Boolean) -> Unit,
    onRefreshIp: () -> Unit
) {
    var peerIpInput by remember { mutableStateOf("") }
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = VibrantSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, VibrantBorder, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (networkState.isBroadcasting) VibrantGreen.copy(alpha = pulseAlpha)
                                    else VibrantPrimary
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "লাইভ নেটওয়ার্ক ভয়েস 🌐",
                            color = VibrantTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VibrantTextSecondary)
                    }
                }

                Text(
                    text = "ফোন থেকে ফোনে সরাসরি কথা বলুন ও শুনুন",
                    color = VibrantTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                // Status Banner
                val statusText = when {
                    networkState.isBroadcasting -> "🎙️ কথা বলছেন (Broadcasting Voice)"
                    networkState.isReceiving -> "🔊 অন্য ফোন থেকে কথা শোনা যাচ্ছে (Receiving Audio)"
                    isUserOnMic && isMuted -> "🔇 মাইক মিউট আছে (Unmute to talk)"
                    isUserOnMic -> "🎙️ সিটে আছেন (Ready to talk)"
                    else -> "🎧 শ্রোতা হিসেবে শুনছেন (Listening Mode)"
                }
                val statusColor = when {
                    networkState.isBroadcasting -> VibrantGreen
                    networkState.isReceiving -> VibrantCyan
                    else -> VibrantPrimary
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Real-time Mic Level / Amplitude meter
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VibrantSurfaceVariant)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মাইক্রোফোন লেভেল (Live Mic Meter):",
                            color = VibrantTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(networkState.localVolume * 100).toInt()}%",
                            color = if (networkState.localVolume > 0.05f) VibrantGreen else VibrantTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress bar representing live audio amplitude
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFFE0E0E0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(networkState.localVolume.coerceIn(0.02f, 1f))
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(VibrantGreen, VibrantCyan, VibrantPrimary)
                                    )
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Network Details (Local IP & UDP Port)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VibrantSurfaceVariant)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "এই ফোনের IP অ্যাড্রেস:",
                            color = VibrantTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = networkState.localIp,
                            color = VibrantTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "UDP Port: ${NetworkVoiceManager.DEFAULT_PORT} (Broadcast Active)",
                            color = VibrantPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(onClick = onRefreshIp, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh IP", tint = VibrantPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Loopback / Hear My Voice test toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VibrantSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "নিজের কণ্ঠ পরীক্ষা (Mic Loopback Test)",
                            color = VibrantTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "মাইক ঠিকঠাক চলছে কিনা স্পিকারে শুনুন",
                            color = VibrantTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = networkState.isLoopbackEnabled,
                        onCheckedChange = onToggleLoopback,
                        colors = SwitchDefaults.colors(checkedThumbColor = VibrantPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Direct Peer IP connection option (for routers blocking broadcast)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VibrantSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "অন্য ফোনের সাথে সরাসরি লিংক (Peer IP):",
                        color = VibrantTextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = peerIpInput,
                            onValueChange = { peerIpInput = it },
                            placeholder = { Text("উদা: 192.168.1.15", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (peerIpInput.isNotBlank()) {
                                    onAddPeerIp(peerIpInput)
                                    peerIpInput = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("যুক্ত করুন", fontSize = 11.sp)
                        }
                    }

                    if (networkState.knownPeerIps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "যুক্ত ফোন: ${networkState.knownPeerIps.joinToString(", ")}",
                            color = VibrantGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Transmission Counters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "পাঠানো অডিও", color = VibrantTextSecondary, fontSize = 10.sp)
                        Text(
                            text = "${networkState.packetsSent} pkts",
                            color = VibrantTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "গৃহীত অডিও", color = VibrantTextSecondary, fontSize = 10.sp)
                        Text(
                            text = "${networkState.packetsReceived} pkts",
                            color = VibrantCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ঠিক আছে (Done)", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
