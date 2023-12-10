package com.ykseon.toastmaster.ui.timer

import com.ykseon.toastmaster.common.DEBATE_ROLE
import com.ykseon.toastmaster.common.EVALUATOR_ROLE
import com.ykseon.toastmaster.common.SPEAKER_ROLE
import com.ykseon.toastmaster.common.TABLE_TOPIC_ROLE

data class TimerRecordItem(val item: TimerItem, val id: Long = -1)
data class TimerItem(val role: String, val cutoffs: String)

val defaultTimerList = arrayListOf(
    TimerItem(SPEAKER_ROLE, "3-4-5"),
    TimerItem(SPEAKER_ROLE, "4-5-6"),
    TimerItem(SPEAKER_ROLE, "5-6-7"),
    TimerItem(SPEAKER_ROLE, "6-7-8"),
    TimerItem(SPEAKER_ROLE, "7-8-9"),
    TimerItem(SPEAKER_ROLE, "8-9-10"),
    TimerItem(SPEAKER_ROLE, "9-10-11"),
    TimerItem(SPEAKER_ROLE, "10-11-12"),
    TimerItem(SPEAKER_ROLE, "11-12-13"),
    TimerItem(TABLE_TOPIC_ROLE, "1-2"),
    TimerItem(EVALUATOR_ROLE, "2-3"),
    TimerItem(DEBATE_ROLE, "1-2"),
    TimerItem(DEBATE_ROLE, "2-3"),
    TimerItem(DEBATE_ROLE, "3-4"),
)
