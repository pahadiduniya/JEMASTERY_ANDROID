package com.jemastery.app.data

data class Folder(
    val id: String,
    val name: String,
    val icon: String,
    val parentId: String,
    val accessType: String
)

data class Section(
    val name: String,
    val start: Int,
    val end: Int
) {
    val count: Int get() = (end - start + 1).coerceAtLeast(0)
}

data class Quiz(
    val id: String,
    val folderId: String,
    val title: String,
    val sequence: Int,
    val accessType: String,
    val timeMinutes: Int,
    val totalQs: Int,
    val isMigrated: Boolean,
    val textData: String,
    val sections: List<Section>,
    val telegramCode: String,
    val levelRating: Double,
    val contentRating: Double
) {
    val isMultiSection: Boolean get() = sections.isNotEmpty()
}

data class Question(
    val text: String,
    val options: List<String>,
    val correct: Int,
    val explanation: String,
    val pyqTag: String?
)

data class HistoryItem(
    val quizId: String,
    val quizTitle: String,
    val correct: Int,
    val wrong: Int,
    val skipped: Int,
    val total: Int,
    val accuracy: Int,
    val timeTaken: String,
    val date: Long
)

data class UserProfile(
    val name: String = "student",
    val username: String = "student",
    val email: String = "",
    val isPremium: Boolean = false,
    val isGuest: Boolean = false,
    val isBanned: Boolean = false,
    val proExpiresAt: Long = 0L
) {
    val tier: Tier get() = when {
        isPremium -> Tier.PRO_PLUS
        isGuest -> Tier.FREE
        else -> Tier.PRO
    }
    val validityDays: Int
        get() {
            if (proExpiresAt <= 0L) return 0
            val diff = proExpiresAt - System.currentTimeMillis()
            return if (diff <= 0) 0 else (diff / (1000 * 60 * 60 * 24)).toInt()
        }
}

enum class Tier(val label: String) {
    FREE("GUEST USER"),
    PRO("PRO"),
    PRO_PLUS("PRO+")
}

/** What a piece of content requires. */
enum class Access(val key: String, val chip: String) {
    FREE("free", "\uD83D\uDFE2 FREE"),
    PRO("ad_supported", "\u25B6 PRO"),
    PRO_PLUS("pro_only", "\uD83D\uDD12 PRO+");

    companion object {
        fun of(raw: String?, isPremiumFlag: Boolean): Access = when (raw) {
            "pro_only" -> PRO_PLUS
            "ad_supported", "pro" -> PRO
            "free" -> FREE
            else -> if (isPremiumFlag) PRO_PLUS else FREE
        }
    }
}

fun canOpen(access: Access, tier: Tier): Boolean = when (access) {
    Access.FREE -> true
    Access.PRO -> tier != Tier.FREE
    Access.PRO_PLUS -> tier == Tier.PRO_PLUS
}

/** True when an ad should be shown before opening. */
fun needsAd(access: Access, tier: Tier): Boolean {
    if (tier == Tier.PRO_PLUS) return false
    return when (access) {
        Access.FREE -> true
        Access.PRO -> true
        Access.PRO_PLUS -> false
    }
}

enum class QuizMode { EXAM, PRACTICE }

enum class Lang(val label: String) {
    ENGLISH("English"),
    HINDI("\u0939\u093F\u0928\u094D\u0926\u0940"),
    BILINGUAL("Bilingual")
}
