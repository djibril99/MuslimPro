package com.example.muslimpro

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var time: String,
    var enabled: Boolean
) {
    companion object {
        fun fromAlarm(alarm: AlarmEntity): AlarmEntity {
            return AlarmEntity(
                time = alarm.time,
                enabled = alarm.enabled
            )
        }
    }
}