package com.ykseon.toastmaster.ui.timer

import com.ykseon.toastmaster.common.ANONYMOUS
import com.ykseon.toastmaster.common.DEBATE_ROLE
import com.ykseon.toastmaster.common.EVALUATOR_ROLE
import com.ykseon.toastmaster.common.SPEAKER_ROLE
import com.ykseon.toastmaster.common.TABLE_TOPIC_ROLE

data class TimerRecordItem(val item: TimerItem, val id: Long = -1)
data class TimerItem(val role: String, val name: String, val cutoffs: String)

val defaultTimerList = arrayListOf(
    TimerItem(SPEAKER_ROLE, ANONYMOUS,"3-4-5"),
    TimerItem(SPEAKER_ROLE, ANONYMOUS,"4-5-6"),
    TimerItem(SPEAKER_ROLE, ANONYMOUS,"5-6-7"),
    TimerItem(SPEAKER_ROLE, ANONYMOUS,"6-7-8"),
    TimerItem(SPEAKER_ROLE, ANONYMOUS,"7-8-9"),
    TimerItem(TABLE_TOPIC_ROLE, ANONYMOUS, "1-2"),
    TimerItem(EVALUATOR_ROLE, ANONYMOUS, "2-3"),
    TimerItem(DEBATE_ROLE, ANONYMOUS, "1-2"),
    TimerItem(DEBATE_ROLE, ANONYMOUS,"2-3"),
    TimerItem(DEBATE_ROLE, ANONYMOUS,"3-4"),
)
