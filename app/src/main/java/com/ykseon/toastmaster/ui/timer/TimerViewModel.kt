package com.ykseon.toastmaster.ui.timer

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ykseon.toastmaster.R
import com.ykseon.toastmaster.common.ANONYMOUS
import com.ykseon.toastmaster.common.SharedState
import com.ykseon.toastmaster.common.SystemTimer
import com.ykseon.toastmaster.model.SettingsPreferences
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_ACCELERATION
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_BEEP_SOUND
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_BUFFER_TIME
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_GREEN_CARD_POLICY
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_SHOW_ANIMATION
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_SHOW_REMAINING_TIME
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_SHOW_TIMER_DETAIL_INFO
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_START_TIMER_IMMEDIATE
import com.ykseon.toastmaster.model.TimeRecord
import com.ykseon.toastmaster.ui.theme.StateGreen
import com.ykseon.toastmaster.ui.theme.StateRed
import com.ykseon.toastmaster.ui.theme.StateYellow
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

private const val TAG = "TimerViewModel"
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val sharedState: SharedState,
    private val settingsPreferences: SettingsPreferences,
    @ApplicationContext context: Context,
): ViewModel() {

    private val defaultCutOffs = arrayListOf(0,0,0,0)
    var cutOffTimes = defaultCutOffs
    private var timeTickUnit = 50
    private var timeMultiply = 1

    private val showRemainingTime = settingsPreferences.getValue(KEY_SHOW_REMAINING_TIME, false)
        .stateIn(viewModelScope, Eagerly, false)
    private val initState
        get() = 0L.toTimerState(cutOffTimes, showRemainingTime.value)

    private val _currentTime = MutableStateFlow(initState)
    val currentTime = _currentTime.asStateFlow()
    private var greenMarginalTime = 0
    private var redMarginalTime = 0

    private lateinit var role: String
    lateinit var name: String
    lateinit var cutoffs: String

    var defaultBackgroundColor: Int = Color.LTGRAY

    val remainingText = showRemainingTime.map {
        if (it) context.resources.getText(R.string.remaining_notification) else ""
    }.stateIn(viewModelScope, Eagerly, "")

    val timeText =
        _currentTime
            .distinctUntilChanged {
                old, new -> old.info.minute == new.info.minute && old.info.second == new.info.second
            }
            .map{it.info.makeTimeString()}
            .stateIn(viewModelScope, WhileSubscribed(), "00:00")

    // region color change
    private val _backgroundColor = MutableStateFlow(calculateStateColor(_currentTime.value))
    val backgroundColor: StateFlow<Int> = _backgroundColor
    private var colorTransitionJob: Job? = null
    // endregion

    var animIconMovingSpan = 0

    private val bufferTime = settingsPreferences.getValue(KEY_BUFFER_TIME, 0)
        .stateIn(viewModelScope, Eagerly, 0)

    private val greenCardPolicy = settingsPreferences.getValue(KEY_GREEN_CARD_POLICY, 0)
        .stateIn(viewModelScope, Eagerly, 0)

    init {
        settingsPreferences.getValue(KEY_ACCELERATION, false).onEach {
            timeMultiply = if (it) 10 else 1
        }.launchIn(viewModelScope)

        showRemainingTime.onEach {
            _currentTime.value =
                _currentTime.value.info.tick.toTimerState(cutOffTimes, it)
        }.launchIn(viewModelScope)

        observeTimeChanges()
    }

    val detailVisible = settingsPreferences
        .getValue(KEY_SHOW_TIMER_DETAIL_INFO, true)
        .stateIn(viewModelScope, WhileSubscribed(), true)

    val animationVisible =settingsPreferences
        .getValue(KEY_SHOW_TIMER_DETAIL_INFO, true)
        .combine(
        settingsPreferences.getValue(KEY_SHOW_ANIMATION, false)
    ) {
        detail, animation -> !detail && animation
    }.stateIn(viewModelScope, WhileSubscribed(), false)

    fun setDetailVisible(value: Boolean) {
        settingsPreferences.saveValue(KEY_SHOW_TIMER_DETAIL_INFO, value)
    }

    fun toggleDetailVisible() {
        settingsPreferences.saveValue(
            KEY_SHOW_TIMER_DETAIL_INFO,
            !detailVisible.value
            )
    }
    fun toggleTimerRemaining() {
        settingsPreferences.saveValue(KEY_SHOW_REMAINING_TIME, !showRemainingTime.value)
    }

    fun tryStart() {
        viewModelScope.launch {
            settingsPreferences.getValueImmediate(KEY_START_TIMER_IMMEDIATE).let {
                if (it == null || it) startButtonClick()
            }
        }
    }
    private fun TimeInfo.makeTimeString(): String {
        return if( minute >= 0L && second >= 0L) {
            "${toTwoDigits(minute)}:${toTwoDigits(second)}"
        }
        else {
            "-${toTwoDigits(abs(minute))}:${toTwoDigits(abs(second))}"
        }
    }

    private fun TimeInfo.makeTimeStringForReport(): String {

        val elapsed = tick / 1000
        val minute = elapsed / 60
        val second = elapsed % 60
        return "${toTwoDigits(minute)}:${toTwoDigits(second)}"
    }

    val progress = _currentTime.map {
        if (cutOffTimes.size == 4) {
            val current = it.info.tick
            val max = cutOffTimes[3] * 1000
            (current.toFloat()/max.toFloat() * 1000F).toInt()
        } else {
            0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val progressValue = _currentTime.map {
        if (cutOffTimes.size == 4) {
            val current = it.info.tick
            val max = cutOffTimes[3] * 1000
            (current.toFloat()/max)
        } else {
            0F
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0F)

    val animationTranslation = _currentTime.map {
        if (cutOffTimes.size == 4) {
            val current = it.info.tick
            val max = cutOffTimes[3] * 1000
            animIconMovingSpan * current / max
        } else {
            0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    val timeTextColor =
        _currentTime.map {
            when (it) {
                is TimerState.Initialized -> Color.BLACK
                else -> Color.WHITE
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), Color.DKGRAY)

    private fun observeTimeChanges() {
        _currentTime
            .onEach { state ->
                colorTransitionJob?.cancel()
                colorTransitionJob = viewModelScope.launch {
                    val startColor = _backgroundColor.value
                    val endColor = calculateStateColor(state)
                    if (startColor != endColor)
                        animateColorTransition(startColor, endColor, 200, 20)
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun animateColorTransition(startColor: Int, endColor: Int, duration: Long, interval: Long) {
        var elapsed = 0L
        while (elapsed < duration) {
            val fraction = elapsed.toFloat() / duration.toFloat()
            val currentColor = mixColors(startColor, endColor, fraction)
            _backgroundColor.value = currentColor
            delay(interval)
            elapsed += interval
        }
        _backgroundColor.value = endColor
    }

    private fun calculateStateColor(state: TimerState): Int {
        return when (state) {
            is TimerState.Initialized -> Color.LTGRAY
            is TimerState.Ready -> Color.DKGRAY
            is TimerState.Green -> StateGreen
            is TimerState.Yellow -> StateYellow
            is TimerState.Red -> StateRed
            is TimerState.Expired -> StateRed
        }
    }

    private fun mixColors(startColor: Int, endColor: Int, fraction: Float): Int {
        val alpha = (Color.alpha(startColor) * (1 - fraction) + Color.alpha(endColor) * fraction).toInt()
        val red = (Color.red(startColor) * (1 - fraction) + Color.red(endColor) * fraction).toInt()
        val green = (Color.green(startColor) * (1 - fraction) + Color.green(endColor) * fraction).toInt()
        val blue = (Color.blue(startColor) * (1 - fraction) + Color.blue(endColor) * fraction).toInt()
        return Color.argb(alpha, red, green, blue)
    }


    private var timerJob: Job? = null

    private fun toTwoDigits(number: Long) ="%02d".format(number)

    private fun start() {
        _currentTime.value = TimerState.ready()
        var timeTick = 0L
        var timePaused = 0L
        val systemTimer = SystemTimer().apply { init() }

        timerJob = viewModelScope.launch(Dispatchers.Default) {
            while(true) {
                delay(timeTickUnit.toLong())
                if (!_currentTime.value.paused) {
                    timeTick = timePaused + systemTimer.getElapsedTime().toInt()
                    timeTick *= timeMultiply
                    _currentTime.value = timeTick.toTimerState(cutOffTimes, showRemainingTime.value)
                } else {
                    timePaused = timeTick
                    systemTimer.init()
                }
            }
        }
    }

    private val _closeTimer = MutableSharedFlow<Unit>()
    val closeTimer = _closeTimer.asSharedFlow()

    private fun stop() {
        timerJob?.cancel()
        // _currentTime.value = initState

        viewModelScope.launch {
            _closeTimer.emit(Unit)
        }
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

    val recordToastShow = MutableSharedFlow<String>()

    private fun TimeInfo.checkQualified(): Boolean {
        val elapsed = tick / 1000

        return if (greenCardPolicy.value == 0) {
            elapsed >= (cutOffTimes[0] - greenMarginalTime) && elapsed <= cutOffTimes[3]
        } else {
            elapsed >= (cutOffTimes[0]) && elapsed <= cutOffTimes[3]
        }
    }

    private fun recordTime() {
        val time = _currentTime.value.info.makeTimeStringForReport()
        val qualified = _currentTime.value.info.checkQualified()

        sharedState.timeRecords.value =
            sharedState.timeRecords.value.plus(
                TimeRecord( role, cutoffs, name, time, qualified)
            )
        viewModelScope.launch {
            val toastString = if (qualified) "$name qualified with a time of $time"
            else "$name didn't qualify with a time of $time"

            recordToastShow.emit(toastString)
        }
    }

    private val _showNameInputDialog = MutableSharedFlow<Unit>()
    val showNameInputDialog = _showNameInputDialog.asSharedFlow()

    fun stopButtonClick() {
        pause()
        if(name == ANONYMOUS) {
            viewModelScope.launch {
                _showNameInputDialog.emit(Unit)
            }
            return
        }
        else {
            recordTime()
        }
        stop()
    }

    fun setRoleAndCutoffs(role: String, name: String, cutoffs: String) {
        this.role = role
        this.name = name
        this.cutoffs = cutoffs

        val array = cutoffs.split("-")
        val endTime = array.last().toInt()
        val bufferTime = when(bufferTime.value) {
            0 -> if (endTime <= 2) 15 else 30
            1 -> 15
            2 -> 30
            3 -> 40
            else -> { 0 }
        }
        greenMarginalTime = bufferTime
        redMarginalTime = bufferTime

        val greenCardMargin = if (greenCardPolicy.value == 2) bufferTime else 0

        cutOffTimes = when (array.size) {
            2 -> {
                val t1 = array[0].toInt() * 60
                val t2 = array[1].toInt() * 60
                arrayListOf(t1 - greenCardMargin , (t1+t2)/2, t2, t2 + bufferTime)
            }
            3 -> {
                val t1 = array[0].toInt() *60
                val t2 = array[1].toInt() *60
                val t3 = array[2].toInt() *60
                arrayListOf(t1 - greenCardMargin, t2, t3, t3 + bufferTime)
            }
            else -> {
                defaultCutOffs
            }
        }

        _currentTime.value = _currentTime.value.info.tick.toTimerState(cutOffTimes, showRemainingTime.value)

    }



    val beepSound = settingsPreferences.getValue(KEY_BEEP_SOUND, false)
        .stateIn(viewModelScope, WhileSubscribed(), false)
}