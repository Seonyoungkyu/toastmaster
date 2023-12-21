package com.ykseon.toastmaster.common

import com.ykseon.toastmaster.model.TimeRecord
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedState @Inject constructor() {
    val timeRecords = MutableStateFlow<List<TimeRecord>>(mutableListOf())
}