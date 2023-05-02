package com.example.muslimpro

import android.content.ContentValues
import android.content.Context
import androidx.lifecycle.LiveData

class AlarmRepository(private val alarmDao: AlarmDao, private val context: Context) {

    fun getAlarms(): List<Alarm> {
        return alarmDao.getAlarms()
    }

    fun addAlarm(time: String): Alarm {
        val alarmEntity = AlarmEntity(time = time, enabled = true)
        val id = (alarmDao.insert(alarmEntity)).toString()
        return Alarm(id = id.toInt(), time = time, enabled = true)

    }

    fun updateAlarm(alarm: AlarmEntity) {
        alarmDao.update(alarm)
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        alarmDao.delete(alarm)
    }
}
