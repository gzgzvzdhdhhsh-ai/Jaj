package com.example.data

enum class RoomCategory(val label: String, val icon: String) {
    POPULAR("Popular", "🔥"),
    BANGLA("Bangla Adda", "🇧🇩"),
    MUSIC("Music & Songs", "🎵"),
    GAMING("Ludo & Games", "🎲"),
    CHILL("Friendship", "💬"),
    NEW("New Voices", "✨")
}

enum class RoomMode {
    NORMAL,
    PK_BATTLE,
    LUDO_PARTY
}

enum class MessageType {
    CHAT,
    SYSTEM,
    GIFT,
    REACTION,
    DICE_ROLL
}

data class UserProfile(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val avatarColorHex: Long,
    val level: Int,
    val vipBadge: String = "VIP 1",
    val wealthLevel: Int = 5,
    val charmLevel: Int = 8,
    val coins: Long = 12500,
    val diamonds: Long = 3400,
    val countryFlag: String = "🇧🇩",
    val countryName: String = "Bangladesh",
    val bio: String = "Voice lover & Ludo player! Let's talk!",
    val isFollowing: Boolean = false,
    val isHost: Boolean = false
)

data class MicSeat(
    val seatIndex: Int,
    val user: UserProfile? = null,
    val isMuted: Boolean = false,
    val isSpeaking: Boolean = false,
    val isLocked: Boolean = false,
    val speakingVolume: Float = 0f
)

data class VoiceRoom(
    val id: String,
    val title: String,
    val host: UserProfile,
    val category: RoomCategory,
    val tags: List<String>,
    val seats: List<MicSeat>,
    val onlineCount: Int,
    val wallpaperRes: String = "room_bg_party",
    val announcement: String = "Welcome to our room! Be kind, make friends, and enjoy the music and games! 🎧",
    val isLocked: Boolean = false,
    val roomMode: RoomMode = RoomMode.NORMAL,
    val pkScoreLeft: Int = 1840,
    val pkScoreRight: Int = 2150
)

data class VirtualGift(
    val id: String,
    val name: String,
    val icon: String,
    val costCoins: Int,
    val colorHex: Long,
    val category: String = "Popular"
)

data class RoomMessage(
    val id: String,
    val senderName: String,
    val senderLevel: Int,
    val senderAvatarColor: Long,
    val content: String,
    val type: MessageType = MessageType.CHAT,
    val giftName: String? = null,
    val giftIcon: String? = null,
    val giftMultiplier: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

data class DirectMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val isVoiceNote: Boolean = false,
    val voiceDurationSec: Int = 0,
    val isMe: Boolean = false,
    val timestamp: String = "Just now"
)

data class Conversation(
    val id: String,
    val friend: UserProfile,
    val lastMessage: String,
    val lastTime: String,
    val unreadCount: Int = 0
)

enum class RoomSoundEffect(val title: String, val icon: String, val freq: Int) {
    APPLAUSE("Applause", "👏", 600),
    CHEER("Cheer", "🎉", 850),
    LAUGHTER("Laugh", "😂", 720),
    HORN("Air Horn", "🎺", 980),
    DRUMROLL("Drumroll", "🥁", 520),
    HEARTBEAT("Heart", "💓", 440)
}
