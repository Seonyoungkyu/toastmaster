package com.ykseon.toastmaster.model

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ykseon.toastmaster.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "SettingsPreferences"
@Singleton
class SettingsPreferences @Inject constructor(
    @ApplicationContext val context: Context,
    @ApplicationScope private val scope: CoroutineScope,
) {
    fun <T> getValue(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return context.dataStore.data.map {
            it[key] ?: defaultValue
        }
    }

    suspend fun <T> getValueImmediate(key: Preferences.Key<T>): T? {
        val dataStoreValue = context.dataStore.data.first()
        val value = dataStoreValue[key]

        // Optional: Log the snapshot to help with debugging
        Log.i(TAG, "DataStore snapshot: $dataStoreValue")

        return value
    }

    fun <T> saveValue(key: Preferences.Key<T>, value: T) {
        scope.launch {
            context.dataStore.edit {
                it[key] = value
            }
        }
    }
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")

        val KEY_SHOW_TIMER_DETAIL_INFO = booleanPreferencesKey("show_timer_detail_info")
        val KEY_START_TIMER_IMMEDIATE = booleanPreferencesKey("start_timer_immediate")
        val KEY_BUFFER_TIME = intPreferencesKey("buffer_time_policy")
        val KEY_GREEN_CARD_POLICY = intPreferencesKey("buffer_time_policy")
        val KEY_SORT_TYPE = intPreferencesKey("sort_type")
        val KEY_ACCELERATION = booleanPreferencesKey("acceleration")
        val KEY_SHOW_REMAINING_TIME = booleanPreferencesKey("show_remaining_time")
    }
}


