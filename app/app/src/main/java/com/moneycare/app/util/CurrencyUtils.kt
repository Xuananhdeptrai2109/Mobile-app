package com.moneycare.app.util

import java.text.NumberFormat
import java.util.Locale

private val vietnameseLocale: Locale = Locale.forLanguageTag("vi-VN")

fun Long.toVnd(): String = "${NumberFormat.getNumberInstance(vietnameseLocale).format(this)} đ"

fun Long.toSignedVnd(isPositive: Boolean): String {
    val sign = if (isPositive) "+" else "-"
    return "$sign${NumberFormat.getNumberInstance(vietnameseLocale).format(this)} đ"
}

fun formatVnd(amount: Long): String {
    val sign = if (amount < 0) "-" else ""
    val grouped = kotlin.math.abs(amount).toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()
    return "$sign$grouped đ"
}

fun formatVndDigitsOnly(amount: Long): String {
    if (amount <= 0L) return ""
    return amount.toString().reversed().chunked(3).joinToString(".").reversed()
}

fun parseVndInput(raw: String): Long = raw.filter(Char::isDigit).toLongOrNull() ?: 0L
