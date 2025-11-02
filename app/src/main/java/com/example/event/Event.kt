package com.example.event

data class Event(
    val id: Long,
    var title: String,
    val dateMillis: Long,
    var timeMillis: Long
)
