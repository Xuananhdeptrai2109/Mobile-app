package com.moneycare.app.data.model

data class GroupTaskSummary(
    val id: String,
    val groupId: String, // Thêm groupId để điều hướng
    val groupName: String,
    val members: List<String>,
    val completed: Int,
    val total: Int,
    val emoji: String = "🦖"
) {
    val progress: Float
        get() = if (total == 0) 0f else (completed.toFloat() / total).coerceIn(0f, 1f)

    val membersLabel: String
        get() = members.joinToString(", ")
}
