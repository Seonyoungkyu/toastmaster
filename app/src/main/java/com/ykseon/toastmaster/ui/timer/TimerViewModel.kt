package com.ykseon.toastmaster.ui.timer

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ykseon.toastmaster.common.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimerViewModel @Inject constructor(
    sharedState: SharedState
): ViewModel() {

    private val defaultCutOffs = arrayListOf<Int>(5,10,15,20)
    private var cutOffTimes = defaultCutOffs
    private val initState = 0.toTimerState(cutOffTimes)
    private var timeTickUnit = 1000
    private val _currentTime = MutableStateFlow<TimerState>(initState)
    val currentTime = _currentTime.asStateFlow()
    val timeText =
        _currentTime
            .map{"${toTwoDigits(it.info.minute)}:${toTwoDigits(it.info.second)}"}
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "00:00")

    init {
        sharedState.testMode.onEach {
            timeTickUnit = if (it) 50 else 1000
        }.launchIn(viewModelScope)
    }

    val progress = _currentTime.map {
        if (cutOffTimes.size == 4) {
            val current = it.info.minute * 60 + it.info.second
            val max = cutOffTimes[3]
            (current.toFloat()/max.toFloat() * 1000F).toInt()
        } else {
            0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val backgroundColor =
        _currentTime.map {
            when (it) {
                is TimerState.Initialized -> Color.WHITE
                is TimerState.Ready -> Color.WHITE
                is TimerState.Green -> Color.GREEN
                is TimerState.Yellow -> Color.YELLOW
                is TimerState.Red -> Color.RED
                is TimerState.Expired -> Color.DKGRAY
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Color.WHITE)

    private var timerJob: Job? = null

    private fun toTwoDigits(number: Int) ="%02d".format(number)

    private fun start() {
        var timeTick = 0
        _currentTime.value = TimerState.ready()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(timeTickUnit.toLong())
                if (!_currentTime.value.paused) {
                    timeTick++
                    _currentTime.value = timeTick.toTimerState(cutOffTimes)
                }
            }
        }
    }

    private fun stop() {
        timerJob?.cancel()
        _currentTime.value = initState
    }

    private fun pause() {
        _currentTime.value = _currentTime.value.copy(true)
    }

    private fun resume() {
        _currentTime.value = _currentTime.value.copy(false)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    fun startButtonClick() {
        if(_currentTime.value is TimerState.Initialized) start()
        else if (_currentTime.value.paused) resume()
        else pause()
    }

    fun stopButtonClick() {
        stop()
    }

    fun setRoleAndCutoffs(role: String, cutoffs: String) {

        val array = cutoffs.split("-")
        val margin = if (role == "TableTopic" || (array.size >=2 && array[0].toInt() == 1) ) 15 else 30

        if (array.size == 2) {
            val t1 = array[0].toInt() *60
            val t2 = array[1].toInt() *60
            cutOffTimes = arrayListOf(t1 - margin, (t1+t2)/2, t2, t2 + margin)
        } else if (array.size == 3) {
            val t1 = array[0].toInt() *60
            val t2 = array[1].toInt() *60
            val t3 = array[2].toInt() *60
            cutOffTimes = arrayListOf(t1- margin, t2, t3, t3 + margin)
        } else {
            cutOffTimes = defaultCutOffs
        }
    }
}