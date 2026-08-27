package com.example.trueline_listener

import java.text.SimpleDateFormat
import java.util.*

actual fun getCurrentTimeFormatted(): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date()).uppercase()
}

actual fun formatTimestamp(isoString: String): String {
    if (isoString.isBlank()) return getCurrentTimeFormatted()
    
    // If it's already in 12-hour format (e.g. 05:20 PM)
    if (isoString.contains("AM") || isoString.contains("PM")) return isoString.uppercase()

    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss"
    )

    for (pattern in patterns) {
        try {
            val inputFormat = SimpleDateFormat(pattern, Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(isoString)
            if (date != null) {
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                return outputFormat.format(date).uppercase()
            }
        } catch (_: Exception) {}
    }

    // Fallback parsing if substring contains ISO T
    return try {
        if (isoString.length >= 16 && isoString.contains("T")) {
            val timePart = isoString.substringAfter("T").take(5)
            val parts = timePart.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toInt()
                val min = parts[1].toInt()
                val ampm = if (hour >= 12) "PM" else "AM"
                val h12 = if (hour % 12 == 0) 12 else hour % 12
                return String.format("%02d:%02d %s", h12, min, ampm)
            }
        }
        isoString
    } catch (_: Exception) {
        isoString
    }
}
