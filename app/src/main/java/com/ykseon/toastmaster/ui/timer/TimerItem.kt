package com.ykseon.toastmaster.ui.timer

import com.ykseon.toastmaster.common.DEBATE_ROLE
import com.ykseon.toastmaster.common.EVALUATOR_ROLE
import com.ykseon.toastmaster.common.SPEAKER_ROLE
import com.ykseon.toastmaster.common.TABLE_TOPIC_ROLE

data class TimerRecordItem(val item: TimerItem, val id: Long = -1)
data class TimerItem(val role: String, val name: String, val cutoffs: String)

val defaultTimerList = arrayListOf(
    TimerItem(SPEAKER_ROLE, "speaker1","3-4-5"),
    TimerItem(SPEAKER_ROLE, "speaker2","4-5-6"),
    TimerItem(SPEAKER_ROLE, "speaker3","5-6-7"),
    TimerItem(SPEAKER_ROLE, "speaker4","6-7-8"),
    TimerItem(TABLE_TOPIC_ROLE, "table topic1", "1-2"),
    TimerItem(TABLE_TOPIC_ROLE, "table topic2", "1-2"),
    TimerItem(TABLE_TOPIC_ROLE, "table topic3", "1-2"),
    TimerItem(TABLE_TOPIC_ROLE, "table topic4", "1-2"),
    TimerItem(TABLE_TOPIC_ROLE, "table topic5", "1-2"),
    TimerItem(EVALUATOR_ROLE, "evaluator1", "2-3"),
    TimerItem(EVALUATOR_ROLE, "evaluator2", "2-3"),
    TimerItem(DEBATE_ROLE, "debater1", "1-2"),
    TimerItem(DEBATE_ROLE, "debater2","2-3"),
    TimerItem(DEBATE_ROLE, "debater3","3-4"),
)
