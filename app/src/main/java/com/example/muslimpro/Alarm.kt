package com.example.muslimpro

data class Alarm(
    val id: Int,
    var time: String,
    var enabled : Boolean = true,
)