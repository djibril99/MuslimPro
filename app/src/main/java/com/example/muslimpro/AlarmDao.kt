package com.example.muslimpro

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AlarmDao {

    @Insert
    fun insert(alarm: AlarmEntity)

    @Update
    fun update(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    fun deleteAlarmById(id: Int)

    @Query("SELECT * FROM alarms")
    fun getAlarms(): LiveData<List<AlarmEntity>>
}
