package com.example.muslimpro

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AlarmDao {

    @Insert
    fun insert(alarm: AlarmEntity)

    @Update
    fun update(alarm: AlarmEntity)

    @Delete
    fun delete(alarm: AlarmEntity)

    @Query("SELECT * FROM alarms ORDER BY time ASC")
    fun getAlarms(): List<Alarm>

}