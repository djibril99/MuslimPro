package com.example.muslimpro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val alarmDao: AlarmDao = AlarmDatabase.getInstance(application).alarmDao()
    val alarms: List<Alarm> = alarmDao.getAlarms()
}