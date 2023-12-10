package com.ykseon.toastmaster.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TimerDAO {
    @Insert
    fun insert(timer: Timer)

    @Query("SELECT * FROM timer")
    fun getAllTimers(): List<Timer>

    @Query("SELECT * FROM timer WHERE id = :id")
    fun getTimerById(id: Int): Timer

    @Update
    fun update(timer: Timer)

    @Delete
    fun delete(timer: Timer)
}