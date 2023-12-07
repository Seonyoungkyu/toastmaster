package com.ykseon.toastmaster.ui.timer

data class TimerItem(val role: String, val cutoffs: String)

val timerList = arrayListOf(
    TimerItem("Speaker", "3-4-5"),
    TimerItem("Speaker", "4-5-6"),
    TimerItem("Speaker", "5-6-7"),
    TimerItem("Speaker", "6-7-8"),
    TimerItem("Speaker", "7-8-9"),
    TimerItem("Speaker", "8-9-10"),
    TimerItem("Speaker", "9-10-11"),
    TimerItem("Speaker", "10-11-12"),
    TimerItem("Speaker", "11-12-13"),
    TimerItem("TableTopic", "1-2"),
    TimerItem("Evaluator", "2-3"),
    TimerItem("Custom", ""),
    TimerItem("Debate", "1-2"),
    TimerItem("Debate", "2-3"),
    TimerItem("Debate", "3-4"),
)
