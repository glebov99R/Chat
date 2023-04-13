package com.example.chat.util

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.TimeZone

fun getCurrentTimeFormatted(): String {
    val formatter = DateTimeFormatter.ofPattern("H:mm")
    val timeZone = TimeZone.getDefault()
    return ZonedDateTime.now(ZoneId.of(timeZone.id)).format(formatter)
}
