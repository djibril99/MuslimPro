package com.example.muslimpro

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Database(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "AlarmDatabase"
        private const val TABLE_NAME = "alarms"
        private const val KEY_ID = "id"
        private const val KEY_TIME = "time"
        private const val KEY_ENABLED = "enabled"
    }

    override fun onCreate(db: SQLiteDatabase) {
        if (!tableExists(TABLE_NAME, db)) {
            val createTableSQL = """
            CREATE TABLE $TABLE_NAME (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                time TEXT NOT NULL,
                enabled INTEGER NOT NULL
            )
        """.trimIndent()
            db.execSQL(createTableSQL)
        }
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Create
    fun addAlarm(time:String): Alarm {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_TIME, time)
        values.put(KEY_ENABLED, 1)
        val id = db.insert(TABLE_NAME, null, values)
        db.close()
        return Alarm(id=id.toInt() , time=time , enabled = true)
    }

    // Read
    fun getAlarm(id: Int): Alarm? {
        val db = this.readableDatabase
        val cursor = db.query(TABLE_NAME, arrayOf(KEY_ID, KEY_TIME, KEY_ENABLED), "$KEY_ID=?", arrayOf(id.toString()), null, null, null, null)
        return if (cursor != null && cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(KEY_ID)
            val timeIndex = cursor.getColumnIndex(KEY_TIME)
            val enabledIndex = cursor.getColumnIndex(KEY_ENABLED)

            if (timeIndex >= 0 && enabledIndex >= 0) {
                val time = cursor.getString(timeIndex)
                val enabled = cursor.getInt(enabledIndex) == 1
                Alarm(cursor.getInt(idIndex), time, enabled)
            } else {
                null
            }
        } else {
            null
        }
    }

    fun getAllAlarms(): List<Alarm> {
        val alarms = ArrayList<Alarm>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val timeIndex = cursor.getColumnIndex(KEY_TIME)
                val enabledIndex = cursor.getColumnIndex(KEY_ENABLED)
                val idIndex = cursor.getColumnIndex(KEY_ID)
                if (idIndex >= 0 && timeIndex >= 0 && enabledIndex >= 0 ) {
                    val id = cursor.getInt(idIndex)
                    val time = cursor.getString(timeIndex)
                    val enabled = cursor.getInt(enabledIndex) == 1
                    val alarm = Alarm(id, time, enabled)
                    alarms.add(alarm)
                }

            } while (cursor.moveToNext())
        }
        cursor?.close()
        db.close()
        return alarms
    }

    // Update
    fun updateAlarm(alarm: Alarm): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_TIME, alarm.time)
        values.put(KEY_ENABLED, if (alarm.enabled) 1 else 0)
        val rowsAffected = db.update(TABLE_NAME, values, "$KEY_ID=?", arrayOf(alarm.id.toString()))
        db.close()
        return rowsAffected
    }

    // Delete
    fun deleteAlarm(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$KEY_ID=?", arrayOf(id.toString()))
        db.close()
    }

    fun deleteAllAlarms() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
        db.close()
    }
    private fun tableExists(tableName: String, db: SQLiteDatabase): Boolean {
        val cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table' AND name='$tableName'", null)
        val tableExists = cursor.moveToFirst()
        cursor.close()
        return tableExists
    }

}
