package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VoiceChatRepository {

    // Current User profile
    private val _currentUser = MutableStateFlow(
        UserProfile(
            id = "user_me",
            name = "Ayan Prince",
            avatarEmoji = "👑",
            avatarColorHex = 0xFFFF2E93,
            level = 15,
            vipBadge = "VIP 3",
            wealthLevel = 9,
            charmLevel = 12,
            coins = 15000,
            diamonds = 4800,
            countryFlag = "🇧🇩",
            countryName = "Bangladesh",
            bio = "Love making friends & playing Ludo! Welcome to my voice world 🎧",
            isFollowing = false,
            isHost = false
        )
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    // Gifts list
    val availableGifts = listOf(
        VirtualGift("gift_rose", "Rose", "🌹", 10, 0xFFFF3366, "Popular"),
        VirtualGift("gift_icecream", "Ice Cream", "🍦", 50, 0xFFFF85A1, "Popular"),
        VirtualGift("gift_love", "Love Heart", "💖", 100, 0xFFFF2A85, "Popular"),
        VirtualGift("gift_ring", "Diamond Ring", "💍", 500, 0xFF00F5D4, "Luxury"),
        VirtualGift("gift_car", "Sports Car", "🏎️", 1200, 0xFFFFD166, "Luxury"),
        VirtualGift("gift_rocket", "Rocket", "🚀", 3000, 0xFF7928CA, "Effects"),
        VirtualGift("gift_crown", "Royal Crown", "👑", 5000, 0xFFFFC107, "Effects"),
        VirtualGift("gift_castle", "Dream Castle", "🏰", 10000, 0xFF00E5FF, "Special"),
        VirtualGift("gift_galaxy", "Galaxy World", "🪐", 20000, 0xFFB388FF, "Special")
    )

    // Initial Live Rooms
    private val _rooms = MutableStateFlow(createInitialRooms())
    val rooms: StateFlow<List<VoiceRoom>> = _rooms.asStateFlow()

    // Current Active Room (if user entered one)
    private val _currentRoom = MutableStateFlow<VoiceRoom?>(null)
    val currentRoom: StateFlow<VoiceRoom?> = _currentRoom.asStateFlow()

    // Current Room Messages
    private val _roomMessages = MutableStateFlow<List<RoomMessage>>(emptyList())
    val roomMessages: StateFlow<List<RoomMessage>> = _roomMessages.asStateFlow()

    // Active Gift Animation overlay trigger
    data class GiftEvent(
        val gift: VirtualGift,
        val sender: String,
        val receiver: String,
        val multiplier: Int = 1
    )
    private val _latestGiftEvent = MutableStateFlow<GiftEvent?>(null)
    val latestGiftEvent: StateFlow<GiftEvent?> = _latestGiftEvent.asStateFlow()

    // Conversations / Direct Messages
    private val _conversations = MutableStateFlow(createInitialConversations())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _directMessages = MutableStateFlow<Map<String, List<DirectMessage>>>(createInitialDirectMessages())
    val directMessages: StateFlow<Map<String, List<DirectMessage>>> = _directMessages.asStateFlow()

    fun enterRoom(room: VoiceRoom) {
        _currentRoom.value = room
        _roomMessages.value = listOf(
            RoomMessage(
                id = "sys_1",
                senderName = "System",
                senderLevel = 99,
                senderAvatarColor = 0xFF7928CA,
                content = "📢 Welcome to ${room.title}! Please follow community guidelines.",
                type = MessageType.SYSTEM
            ),
            RoomMessage(
                id = "sys_2",
                senderName = "Host (${room.host.name})",
                senderLevel = room.host.level,
                senderAvatarColor = room.host.avatarColorHex,
                content = room.announcement,
                type = MessageType.CHAT
            )
        )
    }

    fun leaveCurrentRoom() {
        // If user was on mic, remove from seat
        _currentRoom.value?.let { room ->
            val updatedSeats = room.seats.map { seat ->
                if (seat.user?.id == _currentUser.value.id) {
                    seat.copy(user = null, isSpeaking = false, isMuted = false)
                } else seat
            }
            val updatedRoom = room.copy(seats = updatedSeats)
            updateRoomInList(updatedRoom)
        }
        _currentRoom.value = null
        _roomMessages.value = emptyList()
    }

    fun toggleMyMic() {
        val current = _currentRoom.value ?: return
        val myId = _currentUser.value.id
        val mySeat = current.seats.find { it.user?.id == myId } ?: return

        val newMuted = !mySeat.isMuted
        val updatedSeats = current.seats.map {
            if (it.seatIndex == mySeat.seatIndex) {
                it.copy(isMuted = newMuted, isSpeaking = !newMuted)
            } else it
        }
        val updatedRoom = current.copy(seats = updatedSeats)
        _currentRoom.value = updatedRoom
        updateRoomInList(updatedRoom)
    }

    fun takeMicSeat(seatIndex: Int) {
        val current = _currentRoom.value ?: return
        val me = _currentUser.value

        // Check if already on another seat
        val currentSeatOfMe = current.seats.find { it.user?.id == me.id }

        val updatedSeats = current.seats.map { seat ->
            if (seat.seatIndex == seatIndex && seat.user == null && !seat.isLocked) {
                seat.copy(user = me, isMuted = false, isSpeaking = true, speakingVolume = 0.8f)
            } else if (currentSeatOfMe != null && seat.seatIndex == currentSeatOfMe.seatIndex) {
                seat.copy(user = null, isSpeaking = false)
            } else seat
        }

        val updatedRoom = current.copy(seats = updatedSeats)
        _currentRoom.value = updatedRoom
        updateRoomInList(updatedRoom)

        sendRoomMessage("${me.name} jumped onto Mic Seat #${seatIndex + 1} 🎙️", MessageType.SYSTEM)
    }

    fun leaveMicSeat(seatIndex: Int) {
        val current = _currentRoom.value ?: return
        val updatedSeats = current.seats.map { seat ->
            if (seat.seatIndex == seatIndex) {
                seat.copy(user = null, isSpeaking = false, isMuted = false, speakingVolume = 0f)
            } else seat
        }
        val updatedRoom = current.copy(seats = updatedSeats)
        _currentRoom.value = updatedRoom
        updateRoomInList(updatedRoom)
    }

    fun updateSeatSpeaking(seatIndex: Int, isSpeaking: Boolean, volume: Float) {
        val current = _currentRoom.value ?: return
        val updatedSeats = current.seats.map { seat ->
            if (seat.seatIndex == seatIndex) {
                seat.copy(isSpeaking = isSpeaking, speakingVolume = volume)
            } else seat
        }
        _currentRoom.value = current.copy(seats = updatedSeats)
    }

    fun updateRemoteSeatAssignment(
        seatIndex: Int,
        user: UserProfile?,
        isSpeaking: Boolean = false,
        isMuted: Boolean = false
    ) {
        val current = _currentRoom.value ?: return
        val updatedSeats = current.seats.map { seat ->
            if (seat.seatIndex == seatIndex) {
                seat.copy(
                    user = user,
                    isSpeaking = isSpeaking,
                    isMuted = isMuted,
                    speakingVolume = if (isSpeaking) 0.8f else 0f
                )
            } else seat
        }
        val updatedRoom = current.copy(seats = updatedSeats)
        _currentRoom.value = updatedRoom
        updateRoomInList(updatedRoom)
    }

    fun sendRoomMessage(text: String, type: MessageType = MessageType.CHAT) {
        val me = _currentUser.value
        val msg = RoomMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = me.name,
            senderLevel = me.level,
            senderAvatarColor = me.avatarColorHex,
            content = text,
            type = type
        )
        _roomMessages.update { it + msg }
    }

    fun sendVirtualGift(gift: VirtualGift, receiverSeat: MicSeat?, multiplier: Int = 1) {
        val totalCost = gift.costCoins * multiplier
        val me = _currentUser.value
        if (me.coins < totalCost) {
            sendRoomMessage("⚠️ Insufficient coins! Please recharge in Wallet.", MessageType.SYSTEM)
            return
        }

        // Deduct coins & increase charm
        _currentUser.update {
            it.copy(
                coins = it.coins - totalCost,
                wealthLevel = it.wealthLevel + 1
            )
        }

        val receiverName = receiverSeat?.user?.name ?: _currentRoom.value?.host?.name ?: "All on Stage"

        // Broadcast gift message
        val giftMessage = RoomMessage(
            id = "gift_${System.currentTimeMillis()}",
            senderName = me.name,
            senderLevel = me.level,
            senderAvatarColor = me.avatarColorHex,
            content = "sent ${gift.name} ${gift.icon} x$multiplier to $receiverName!",
            type = MessageType.GIFT,
            giftName = gift.name,
            giftIcon = gift.icon,
            giftMultiplier = multiplier
        )
        _roomMessages.update { it + giftMessage }

        // Trigger full screen visual animation
        _latestGiftEvent.value = GiftEvent(gift, me.name, receiverName, multiplier)
    }

    fun clearLatestGiftEvent() {
        _latestGiftEvent.value = null
    }

    fun createRoom(title: String, category: RoomCategory, tag: String, wallpaper: String) {
        val me = _currentUser.value
        val newRoom = VoiceRoom(
            id = "room_${System.currentTimeMillis()}",
            title = title,
            host = me.copy(isHost = true),
            category = category,
            tags = listOf(tag, "Active", "Voice"),
            seats = List(8) { index ->
                if (index == 0) MicSeat(0, me, isMuted = false, isSpeaking = true)
                else MicSeat(index)
            },
            onlineCount = 1,
            wallpaperRes = wallpaper,
            announcement = "Welcome to $title! Let's chat and have fun!"
        )
        _rooms.update { listOf(newRoom) + it }
        enterRoom(newRoom)
    }

    fun rechargeCoins(amount: Long) {
        _currentUser.update { it.copy(coins = it.coins + amount) }
    }

    fun exchangeDiamonds(diamondAmount: Long) {
        _currentUser.update {
            if (it.diamonds >= diamondAmount) {
                it.copy(
                    diamonds = it.diamonds - diamondAmount,
                    coins = it.coins + (diamondAmount * 2)
                )
            } else it
        }
    }

    fun sendDirectMessage(conversationId: String, text: String, isVoice: Boolean = false, voiceSec: Int = 0) {
        val me = _currentUser.value
        val newMsg = DirectMessage(
            id = "dm_${System.currentTimeMillis()}",
            conversationId = conversationId,
            senderId = me.id,
            senderName = me.name,
            text = text,
            isVoiceNote = isVoice,
            voiceDurationSec = voiceSec,
            isMe = true,
            timestamp = "Just now"
        )
        _directMessages.update { map ->
            val list = map[conversationId] ?: emptyList()
            map + (conversationId to (list + newMsg))
        }

        // Update conversation preview
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) {
                    conv.copy(lastMessage = if (isVoice) "🎤 Voice message (${voiceSec}s)" else text, lastTime = "Just now")
                } else conv
            }
        }
    }

    private fun updateRoomInList(updatedRoom: VoiceRoom) {
        _rooms.update { list ->
            list.map { if (it.id == updatedRoom.id) updatedRoom else it }
        }
    }

    private fun createInitialRooms(): List<VoiceRoom> {
        val host1 = UserProfile("u1", "Sara Queen", "👑", 0xFFFF2E93, 24, "VIP 6", 18, 25, 54000, 18000, "🇧🇩", "Bangladesh", "Singer & Host")
        val host2 = UserProfile("u2", "Kabir Rocker", "🎸", 0xFF00F5D4, 21, "VIP 4", 15, 19, 32000, 9500, "🇧🇩", "Bangladesh", "Guitar & Live Songs")
        val host3 = UserProfile("u3", "Ludo Champion", "🎲", 0xFFFFD166, 19, "VIP 3", 14, 16, 22000, 6000, "🇮🇳", "India", "Ludo Voice Party")
        val host4 = UserProfile("u4", "Zara Moonlight", "✨", 0xFFB388FF, 28, "VIP 8", 26, 32, 120000, 45000, "🇸🇦", "Saudi Arabia", "Night Radio & Chill")
        val host5 = UserProfile("u5", "Farhan Bhai", "🎧", 0xFF00E676, 17, "VIP 2", 11, 14, 18000, 4200, "🇧🇩", "Bangladesh", "Late Night Adda")

        return listOf(
            VoiceRoom(
                id = "room_1",
                title = "🇧🇩 বাংলা আড্ডা & গান বাজনা | Dhaka Beats",
                host = host1,
                category = RoomCategory.BANGLA,
                tags = listOf("Bangla", "Songs", "Adda"),
                seats = listOf(
                    MicSeat(0, host1, isMuted = false, isSpeaking = true, speakingVolume = 0.9f),
                    MicSeat(1, UserProfile("u11", "Tanvir", "🎤", 0xFF64B5F6, 14), isMuted = false, isSpeaking = true, speakingVolume = 0.6f),
                    MicSeat(2, UserProfile("u12", "Mithila", "🌸", 0xFFFF80AB, 16), isMuted = true),
                    MicSeat(3, UserProfile("u13", "Rafi", "😎", 0xFFFFD54F, 12), isMuted = false, isSpeaking = false),
                    MicSeat(4), MicSeat(5), MicSeat(6), MicSeat(7)
                ),
                onlineCount = 142,
                announcement = "সবাইকে স্বাগতম! গান শুনুন, কথা বলুন এবং নতুন বন্ধু বানান 🎶"
            ),
            VoiceRoom(
                id = "room_2",
                title = "🎲 Ludo King 4v4 Voice Battle & Coins!",
                host = host3,
                category = RoomCategory.GAMING,
                tags = listOf("Ludo", "Tournament", "Coins"),
                seats = listOf(
                    MicSeat(0, host3, isMuted = false, isSpeaking = true, speakingVolume = 0.7f),
                    MicSeat(1, UserProfile("u21", "Aryan", "🔥", 0xFFFF5722, 18), isMuted = false, isSpeaking = false),
                    MicSeat(2, UserProfile("u22", "Priya", "💎", 0xFF00E5FF, 20), isMuted = false, isSpeaking = true, speakingVolume = 0.5f),
                    MicSeat(3, UserProfile("u23", "Shakil", "🎯", 0xFF81C784, 15), isMuted = true),
                    MicSeat(4), MicSeat(5), MicSeat(6), MicSeat(7)
                ),
                onlineCount = 98,
                roomMode = RoomMode.LUDO_PARTY,
                announcement = "Ludo tournament starting soon! Join seat to play and roll dice 🎲"
            ),
            VoiceRoom(
                id = "room_3",
                title = "🎵 Bollywood & Acoustic Guitar Night Live",
                host = host2,
                category = RoomCategory.MUSIC,
                tags = listOf("Acoustic", "Guitar", "Singing"),
                seats = listOf(
                    MicSeat(0, host2, isMuted = false, isSpeaking = true, speakingVolume = 0.95f),
                    MicSeat(1, UserProfile("u31", "Arijit Fan", "🎼", 0xFFBA68C8, 22), isMuted = false, isSpeaking = false),
                    MicSeat(2), MicSeat(3), MicSeat(4), MicSeat(5), MicSeat(6), MicSeat(7)
                ),
                onlineCount = 215,
                announcement = "Drop your song requests in chat! Playing live guitar 🎸"
            ),
            VoiceRoom(
                id = "room_4",
                title = "🌙 Late Night Radio & Deep Heart Talks",
                host = host4,
                category = RoomCategory.CHILL,
                tags = listOf("Chill", "Podcast", "Friendship"),
                seats = listOf(
                    MicSeat(0, host4, isMuted = false, isSpeaking = true, speakingVolume = 0.8f),
                    MicSeat(1, UserProfile("u41", "Noor", "🌙", 0xFF4DD0E1, 25), isMuted = false, isSpeaking = true, speakingVolume = 0.4f),
                    MicSeat(2), MicSeat(3), MicSeat(4), MicSeat(5), MicSeat(6), MicSeat(7)
                ),
                onlineCount = 330,
                announcement = "A safe space to share your day, feelings, and relax with calming vibes 🕯️"
            ),
            VoiceRoom(
                id = "room_5",
                title = "🔥 PK Battle! Team Dhaka vs Team Chittagong",
                host = host5,
                category = RoomCategory.POPULAR,
                tags = listOf("PK", "Battle", "Gift War"),
                seats = listOf(
                    MicSeat(0, host5, isMuted = false, isSpeaking = true, speakingVolume = 0.85f),
                    MicSeat(1, UserProfile("u51", "Ctg Star", "⚡", 0xFFFFD700, 23), isMuted = false, isSpeaking = true, speakingVolume = 0.75f),
                    MicSeat(2), MicSeat(3), MicSeat(4), MicSeat(5), MicSeat(6), MicSeat(7)
                ),
                onlineCount = 512,
                roomMode = RoomMode.PK_BATTLE,
                pkScoreLeft = 4520,
                pkScoreRight = 5190,
                announcement = "Send rockets and crowns to boost your favorite team! 🚀"
            )
        )
    }

    private fun createInitialConversations(): List<Conversation> {
        val sara = UserProfile("u1", "Sara Queen", "👑", 0xFFFF2E93, 24, "VIP 6", 18, 25, 54000, 18000, "🇧🇩", "Bangladesh")
        val kabir = UserProfile("u2", "Kabir Rocker", "🎸", 0xFF00F5D4, 21, "VIP 4", 15, 19, 32000, 9500, "🇧🇩", "Bangladesh")
        val priya = UserProfile("u22", "Priya", "💎", 0xFF00E5FF, 20, "VIP 5", 16, 20, 41000, 14000, "🇮🇳", "India")

        return listOf(
            Conversation("conv_1", sara, "Thanks for the lovely rose in my room! 🌹", "10m ago", 1),
            Conversation("conv_2", kabir, "Let's play acoustic duet tonight on mic 2 🎸", "1h ago", 0),
            Conversation("conv_3", priya, "Are you ready for Ludo game round 2? 🎲", "Yesterday", 0)
        )
    }

    private fun createInitialDirectMessages(): Map<String, List<DirectMessage>> {
        return mapOf(
            "conv_1" to listOf(
                DirectMessage("m1", "conv_1", "user_me", "Ayan Prince", "Hi Sara! Loved your singing today!", false, 0, true, "20m ago"),
                DirectMessage("m2", "conv_1", "u1", "Sara Queen", "Voice message", true, 6, false, "15m ago"),
                DirectMessage("m3", "conv_1", "u1", "Sara Queen", "Thanks for the lovely rose in my room! 🌹", false, 0, false, "10m ago")
            ),
            "conv_2" to listOf(
                DirectMessage("m4", "conv_2", "u2", "Kabir Rocker", "Hey buddy! You coming to the guitar room?", false, 0, false, "2h ago"),
                DirectMessage("m5", "conv_2", "user_me", "Ayan Prince", "Yes! Joining right now.", false, 0, true, "1h ago"),
                DirectMessage("m6", "conv_2", "u2", "Kabir Rocker", "Let's play acoustic duet tonight on mic 2 🎸", false, 0, false, "1h ago")
            ),
            "conv_3" to listOf(
                DirectMessage("m7", "conv_3", "u22", "Priya", "Good game earlier! That six was clutch 😂", false, 0, false, "Yesterday"),
                DirectMessage("m8", "conv_3", "u22", "Priya", "Are you ready for Ludo game round 2? 🎲", false, 0, false, "Yesterday")
            )
        )
    }
}
