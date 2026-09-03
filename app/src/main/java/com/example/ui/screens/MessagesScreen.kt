package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioEffectManager
import com.example.data.Conversation
import com.example.data.DirectMessage
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun MessagesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.conversations.collectAsState()
    val activeConversation by viewModel.activeConversation.collectAsState()
    val allMessages by viewModel.directMessages.collectAsState()

    if (activeConversation != null) {
        val conv = activeConversation!!
        val msgs = allMessages[conv.id] ?: emptyList()

        PrivateChatScreen(
            conversation = conv,
            messages = msgs,
            onSendMessage = { text ->
                viewModel.sendDirectMessage(conv.id, text, isVoice = false)
            },
            onSendVoice = { duration ->
                viewModel.sendDirectMessage(conv.id, "Voice Message", isVoice = true, voiceSec = duration)
            },
            onBack = { viewModel.closeConversation() }
        )
    } else {
        ConversationListScreen(
            conversations = conversations,
            onSelectConversation = { viewModel.openConversation(it) },
            modifier = modifier
        )
    }
}

@Composable
fun ConversationListScreen(
    conversations: List<Conversation>,
    onSelectConversation: (Conversation) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Messages & Friends 💬",
                    color = VibrantTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = {}) {
                    Icon(Icons.Default.GroupAdd, contentDescription = "Add Friends", tint = VibrantPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        items(conversations) { conv ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = VibrantSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .border(1.dp, VibrantBorder, RoundedCornerShape(18.dp))
                    .clickable { onSelectConversation(conv) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Friend Avatar
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(conv.friend.avatarColorHex).copy(alpha = 0.25f))
                            .border(1.5.dp, VibrantGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = conv.friend.avatarEmoji, fontSize = 24.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = conv.friend.name,
                                color = VibrantTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = conv.lastTime,
                                color = VibrantTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = conv.lastMessage,
                                color = VibrantTextSecondary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            if (conv.unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(VibrantPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${conv.unreadCount}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivateChatScreen(
    conversation: Conversation,
    messages: List<DirectMessage>,
    onSendMessage: (String) -> Unit,
    onSendVoice: (Int) -> Unit,
    onBack: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var isRecordingVoice by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VibrantSurfaceVariant)
                .border(1.dp, VibrantBorder)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VibrantTextPrimary)
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(conversation.friend.avatarColorHex).copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = conversation.friend.avatarEmoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = conversation.friend.name,
                    color = VibrantTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "🟢 Online • ${conversation.friend.vipBadge}",
                    color = VibrantPrimary,
                    fontSize = 11.sp
                )
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.isMe
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 260.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                )
                            )
                            .background(if (isMe) VibrantPrimary else VibrantSurface)
                            .border(1.dp, if (isMe) Color.Transparent else VibrantBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        if (msg.isVoiceNote) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    AudioEffectManager.playSoundEffect(650, 200)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Voice",
                                    tint = if (isMe) Color.White else VibrantPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Voice Note (${msg.voiceDurationSec}s) 🎵",
                                    color = if (isMe) Color.White else VibrantTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = msg.text,
                                color = if (isMe) Color.White else VibrantTextPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Input Row with Voice Recording button & Text Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VibrantSurfaceVariant)
                .border(1.dp, VibrantBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice record button
            IconButton(
                onClick = {
                    isRecordingVoice = !isRecordingVoice
                    if (!isRecordingVoice) {
                        onSendVoice(4)
                        AudioEffectManager.playSoundEffect(800, 100)
                    } else {
                        AudioEffectManager.playSoundEffect(500, 100)
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isRecordingVoice) VibrantLiveRed else VibrantSurface)
                    .border(1.dp, VibrantBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Record Voice",
                    tint = if (isRecordingVoice) Color.White else VibrantPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = if (isRecordingVoice) "Recording voice note..." else "Type message...",
                        color = VibrantTextMuted,
                        fontSize = 13.sp
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = VibrantTextPrimary,
                    unfocusedTextColor = VibrantTextPrimary,
                    focusedBorderColor = VibrantPrimary,
                    unfocusedBorderColor = VibrantBorder,
                    focusedContainerColor = VibrantSurface,
                    unfocusedContainerColor = VibrantSurface
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(VibrantPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
