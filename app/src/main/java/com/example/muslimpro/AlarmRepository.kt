package com.example.muslimpro

import android.content.ContentValues
import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlarmRepository(private val alarmDao: AlarmDao, private val context: Context) {

    fun getAlarms(): LiveData<List<AlarmEntity>> {
        return alarmDao.getAlarms()
    }

    suspend fun addAlarm(time: String): AlarmEntity {
//        val alarmEntity = AlarmEntity(time = time, enabled = true)
//        val id = (alarmDao.insert(alarmEntity)).toString()
//        return AlarmEntity(id = id.toInt(), time = time, enabled = true)
        val newAlarm = AlarmEntity(time = time, enabled = true)
        withContext(Dispatchers.IO) {
            alarmDao.insert(AlarmEntity.fromAlarm(newAlarm))
        }
        return newAlarm
    }

    fun updateAlarm(alarm: AlarmEntity) {
        alarmDao.update(alarm)
    }

    fun deleteAlarm(id : Int) {
        println("Deleting alarm with id: $id")
        alarmDao.deleteAlarmById(id)
    }
}
