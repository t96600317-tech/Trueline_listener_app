package com.example.trueline_listener

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.timeZoneWithAbbreviation

actual fun getCurrentTimeFormatted(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "hh:mm a"
    return formatter.stringFromDate(NSDate()).uppercase()
}

actual fun formatTimestamp(isoString: String): String {
    if (isoString.isBlank()) return getCurrentTimeFormatted()
    if (isoString.contains("AM") || isoString.contains("PM")) return isoString.uppercase()

    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"
    formatter.timeZone = NSTimeZone.timeZoneWithAbbreviation("UTC")
    val date = formatter.dateFromString(isoString)
    if (date != null) {
        val outFormatter = NSDateFormatter()
        outFormatter.dateFormat = "hh:mm a"
        return outFormatter.stringFromDate(date).uppercase()
    }
    return isoString
}
