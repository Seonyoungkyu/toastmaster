package com.ykseon.toastmaster.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Timer (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: String,
    val cutoffs: String
)