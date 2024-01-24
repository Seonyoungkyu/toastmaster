package com.ykseon.toastmaster.ui.preference

import androidx.datastore.preferences.core.Preferences
import com.ykseon.toastmaster.di.ApplicationScope
import com.ykseon.toastmaster.model.SettingsPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceUtil @Inject constructor(
    private val settingsPreferences: SettingsPreferences,
    @ApplicationScope private val scope: CoroutineScope
){
    fun initPreferences() {
        scope.launch {
            initValue(SettingsPreferences.KEY_SHOW_ANIMATION, true)
        }
    }

    private suspend fun <T : Any> initValue(key: Preferences.Key<T>, value: T) {
        if (settingsPreferences.getValueImmediate(key) == null) {
            settingsPreferences.setValueImmediate(key, value)
        }
    }
}