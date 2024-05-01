package com.ykseon.toastmaster.common

class SystemTimer {

    private var startTime = 0L

    fun init() {
        startTime = System.currentTimeMillis()
    }

    fun getElapsedTime(): Long {
        return  System.currentTimeMillis() - startTime
    }
}