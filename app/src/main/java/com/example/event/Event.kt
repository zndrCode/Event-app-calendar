package com.example.event

data class Event(
    val id: Long,
    var title: String,
    val dateMillis: Long,
    var timeMillis: Long,
    var notificationEnabled: Boolean = true, // Add this field
    var notificationMinutesBefore: Int = 15  // Add this field
)