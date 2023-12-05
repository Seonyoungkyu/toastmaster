package com.ykseon.toastmaster.ui.timer

data class TimeInfo (var minute: Int = 0, var second: Int = 0)

sealed class TimerState {
    abstract var paused: Boolean
    abstract val info: TimeInfo
    abstract fun copy(paused: Boolean): TimerState
    data class Initialized(override val info: TimeInfo, override var paused: Boolean = true) : TimerState() {
        override fun copy(paused: Boolean) = copy(paused = paused, info = info)
    }
    data class Ready(override val info: TimeInfo, override var paused: Boolean = false) : TimerState() {
        override fun copy(paused: Boolean) = copy(paused = paused, info = info)
    }
    data class Green(override val info: TimeInfo, override var paused: Boolean = false) : TimerState() {
        override fun copy(paused: Boolean) = copy(paused = paused, info = info)
    }
    data class Yellow(override val info: TimeInfo, override var paused: Boolean = false) : TimerState() {
        override fun copy(paused: Boolean) = copy(paused = paused, info = info)
    }
    data class Red(override val info: TimeInfo, override var paused: Boolean = false) : TimerState() {
        override fun copy(paused: Boolean) = copy(paused = paused, info = info)
    }
    data class Expired(override val info: TimeInfo, override var paused: Boolean = false) : TimerState() {
        override fun copy(paused: Boolean) = copy(paused = paused, info = info)
    }
    companion object {
        fun ready() = Ready(TimeInfo())
    }
}

fun Int.toTimerState(cutoffs: List<Int>): TimerState {
    val minute = this / 60
    val second = this % 60
    val timeInfo = TimeInfo(minute, second)
    return if (this == 0) TimerState.Initialized(timeInfo)
        else if (this < cutoffs[0]) TimerState.Ready(timeInfo)
        else if (this < cutoffs[1]) TimerState.Green(timeInfo)
        else if (this < cutoffs[2]) TimerState.Yellow(timeInfo)
        else if (this < cutoffs[3]) TimerState.Red(timeInfo)
        else TimerState.Expired(timeInfo)
}
