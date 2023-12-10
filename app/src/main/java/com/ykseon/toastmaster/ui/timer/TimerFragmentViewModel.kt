package com.ykseon.toastmaster.ui.timer

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ykseon.toastmaster.common.CREATION_SYSTEM_ROLE
import com.ykseon.toastmaster.common.DEBATE_ROLE
import com.ykseon.toastmaster.common.EVALUATOR_ROLE
import com.ykseon.toastmaster.common.SPEAKER_ROLE
import com.ykseon.toastmaster.common.TABLE_TOPIC_ROLE
import com.ykseon.toastmaster.data.Timer
import com.ykseon.toastmaster.data.TimerDatabase
import com.ykseon.toastmaster.ui.theme.PastelBlue1
import com.ykseon.toastmaster.ui.theme.PastelBlue2
import com.ykseon.toastmaster.ui.theme.PastelBlue3
import com.ykseon.toastmaster.ui.theme.PastelBlue4
import com.ykseon.toastmaster.ui.theme.PastelDarkBlue1
import com.ykseon.toastmaster.ui.theme.PastelDarkBlue2
import com.ykseon.toastmaster.ui.theme.PastelDarkPink2
import com.ykseon.toastmaster.ui.theme.PastelDarkYellow1
import com.ykseon.toastmaster.ui.theme.PastelPink1
import com.ykseon.toastmaster.ui.theme.PastelPink2
import com.ykseon.toastmaster.ui.theme.PastelYellow1
import com.ykseon.toastmaster.ui.theme.darkColors
import com.ykseon.toastmaster.ui.theme.lightColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TimerFragmentViewModel"
@HiltViewModel
class TimerFragmentViewModel @Inject constructor() : ViewModel() {
    fun startTimer(context: Context, role:String, cutoffs: String) {
        startActivity(
            context,
            Intent(context, TimerActivity::class.java).apply {
                putExtra("role", role)
                putExtra("cutoffs", cutoffs)
            },
            null
        )
    }

    private val _timerList: MutableStateFlow<List<TimerRecordItem>> = MutableStateFlow(arrayListOf())
    val timerList = _timerList.asStateFlow()

    private var creationJob: Job? = null
    var fragmentManager: FragmentManager? = null
    private var database: TimerDatabase? = null
    private suspend fun initDatabase(context: Context) {
        database = Room.databaseBuilder(
            context, TimerDatabase::class.java, "timer_database")
            .addCallback(object: RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)

                    creationJob = viewModelScope.launch(Dispatchers.IO) {
                        insertDefaultRecords(context, database)
                    }
                }
            })
            .build()
    }

    private suspend fun insertDefaultRecords(context: Context, database: TimerDatabase?) {
        val timerDAO = database?.timerDAO()

        timerDAO?.let { dao ->
            defaultTimerList.forEach {
                dao.insert(Timer(role = it.role, cutoffs = it.cutoffs))
            }
        }

        loadTimer(context)
    }

    fun loadTimer(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            initDatabase(context)
            val dao = database?.timerDAO()
            val list = mutableListOf<TimerRecordItem>()
            dao?.getAllTimers()?.mapTo(list) {
                TimerRecordItem(TimerItem(it.role, it.cutoffs), it.id)
            }
            list.add(TimerRecordItem(TimerItem("",""), -1))
            _timerList.value = list
        }
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            database?.timerDAO()?.let { dao ->
                _timerList.value.firstOrNull{ it.id == id}?.let {
                    dao.delete(Timer(it.id, it.item.role, it.item.cutoffs))
                }
                // _timerList.value = _timerList.value.minus(it)
                logItems(id)

                val list = mutableListOf<TimerRecordItem>()
                dao.getAllTimers().mapTo(list) {
                    TimerRecordItem(TimerItem(it.role, it.cutoffs), it.id)
                }
                list.add(TimerRecordItem(TimerItem("",""), -1))
                _timerList.value = list
                }
        }
    }

    private fun logItems(id: Long) {
        var output = ""
        _timerList.value.forEach {
            output += "${it.id.toString()};"
        }
        Log.i(TAG,"TimerList - delete($id), current($output)")
    }
    fun createItem(role: String, cutoffs: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database?.timerDAO()?.let { dao ->
                dao.insert(Timer(role = role, cutoffs = cutoffs))
                val list = mutableListOf<TimerRecordItem>()
                dao.getAllTimers().mapTo(list) {
                    TimerRecordItem(TimerItem(it.role, it.cutoffs), it.id)
                }
                list.add(TimerRecordItem(TimerItem("",""), -1))
                _timerList.value = list
            }
        }
    }
    fun getBgColor(role: String, darkMode: Boolean, shuffle: Int = 0): Color {
        return if (!darkMode) when(role) {
            SPEAKER_ROLE -> PastelBlue2
            DEBATE_ROLE -> PastelPink2
            EVALUATOR_ROLE -> PastelYellow1
            TABLE_TOPIC_ROLE -> PastelBlue1
            CREATION_SYSTEM_ROLE -> Color.LightGray
            else -> lightColors[ shuffle % lightColors.size]
        } else when(role) {
            SPEAKER_ROLE -> PastelDarkBlue2
            DEBATE_ROLE -> PastelDarkPink2
            EVALUATOR_ROLE -> PastelDarkYellow1
            TABLE_TOPIC_ROLE -> PastelDarkBlue1
            CREATION_SYSTEM_ROLE -> Color.DarkGray
            else -> darkColors[ shuffle % darkColors.size]
        }
    }

    fun createTimer() {
        val dialog = CustomTimerDialog(object: CustomTimerDialogCallback {
            override fun onYesButtonClick(role: String, cutOffs: String) {
                createItem(role, cutOffs)
            }
        })
        dialog.show(fragmentManager!!, "Timer Input Dialog")
    }
}

