package com.example.muslimpro;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

public class MyDatabaseCallback extends SupportSQLiteOpenHelper.Callback {
    public MyDatabaseCallback(int version) {
        super(version);
    }

    @Override
    public void onCreate(SupportSQLiteDatabase db) {
        // Code to create the database tables goes here
    }

    @Override
    public void onUpgrade(SupportSQLiteDatabase db, int oldVersion, int newVersion) {
        // Code to upgrade the database tables goes here
    }
}
