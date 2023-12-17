package com.ykseon.toastmaster.ui.preference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ykseon.toastmaster.model.SettingsPreferences
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_ACCELERATION
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_BUFFER_TIME
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_GREEN_CARD_POLICY
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_SHOW_REMAINING_TIME
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_SHOW_TIMER_DETAIL_INFO
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_SORT_TYPE
import com.ykseon.toastmaster.model.SettingsPreferences.Companion.KEY_START_TIMER_IMMEDIATE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PreferencesViewModel @Inject constructor(
    private val settingsPreferences: SettingsPreferences,
) : ViewModel() {

    // region show timer details
    val showTimerDetails =
        settingsPreferences
            .getValue(KEY_SHOW_TIMER_DETAIL_INFO, true)
            .stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun setShowTimerDetails(value: Boolean) {
        settingsPreferences.saveValue(KEY_SHOW_TIMER_DETAIL_INFO, value)
    }
    // endregion

    // region start timer immediately
    val startTimerImmediate =
        settingsPreferences
            .getValue(KEY_START_TIMER_IMMEDIATE, true)
            .stateIn(viewModelScope, SharingStarted.Lazily, true)
    fun setStartTimerImmediate(value: Boolean) {
        settingsPreferences.saveValue(KEY_START_TIMER_IMMEDIATE, value)
    }
    // endregion

    // region buffer time immediately
    val bufferTime =
        settingsPreferences
            .getValue(KEY_BUFFER_TIME, 0)
            .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun setBufferTime(value: Int) {
        settingsPreferences.saveValue(KEY_BUFFER_TIME, value)
    }
    // endregion

    // region green card policy
    val greenCardPolicy =
        settingsPreferences
            .getValue(KEY_GREEN_CARD_POLICY, 0)
            .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun setGreenCardPolicy(value: Int) {
        settingsPreferences.saveValue(KEY_GREEN_CARD_POLICY, value)
    }
    // endregion

    // region sort
    val sortType =
        settingsPreferences
            .getValue(KEY_SORT_TYPE, 0)
            .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun setSortType(value: Int) {
        settingsPreferences.saveValue(KEY_SORT_TYPE, value)
    }
    // endregion

    // region accelerate
    val acceleration =
        settingsPreferences
            .getValue(KEY_ACCELERATION, false)
            .stateIn(viewModelScope, SharingStarted.Lazily, false)
    fun setAcceleration(value: Boolean) {
        settingsPreferences.saveValue(KEY_ACCELERATION, value)
    }
    // endregion

    // region accelerate
    val showRemainingTime =
        settingsPreferences
            .getValue(KEY_SHOW_REMAINING_TIME, false)
            .stateIn(viewModelScope, SharingStarted.Lazily, false)
    fun setShowRemainingTime(value: Boolean) {
        settingsPreferences.saveValue(KEY_SHOW_REMAINING_TIME, value)
    }
    // endregion

}