package com.ykseon.toastmaster.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Timer::class], version = 1)
abstract class TimerDatabase : RoomDatabase() {

    abstract fun timerDAO(): TimerDAO
}