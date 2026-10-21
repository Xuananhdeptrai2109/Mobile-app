package com.moneycare.app.util

fun getInitials(name: String): String {
    if (name.isBlank()) return "?"
    val words = name.trim().split("\\s+".toRegex())
    return if (words.size == 1) {
        words[0].take(1).uppercase()
    } else {
        (words[0].take(1) + words.last().take(1)).uppercase()
    }
}
