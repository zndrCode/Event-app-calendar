package com.example.event

import java.io.Serializable

data class Event(
    val id: Long,
    var title: String,
    var description: String = "",
    var dateMillis: Long,
    var timeMillis: Long,
    var location: String = "",
    var isAllDay: Boolean = false,
    var endTimeMillis: Long = 0L,
    var color: Int = 0xFF2196F3.toInt(),
    var reminderMinutes: Int = 0 // 0 = no reminder, 15, 30, 60 minutes before
) : Serializable