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
import com.ykseon.toastmaster.R
import com.ykseon.toastmaster.common.CREATION_SYSTEM_ROLE
import com.ykseon.toastmaster.common.DEBATE_ROLE
import com.ykseon.toastmaster.common.EVALUATOR_ROLE
import com.ykseon.toastmaster.common.SPEAKER_ROLE
import com.ykseon.toastmaster.common.SharedState
import com.ykseon.toastmaster.common.SortOption
import com.ykseon.toastmaster.common.TABLE_TOPIC_ROLE
import com.ykseon.toastmaster.data.Timer
import com.ykseon.toastmaster.data.TimerDAO
import com.ykseon.toastmaster.data.TimerDatabase
import com.ykseon.toastmaster.ui.theme.PastelBlue1
import com.ykseon.toastmaster.ui.theme.PastelBlue2
import com.ykseon.toastmaster.ui.theme.PastelDarkBlue1
import com.ykseon.toastmaster.ui.theme.PastelDarkBlue2
import com.ykseon.toastmaster.ui.theme.PastelDarkPink2
import com.ykseon.toastmaster.ui.theme.PastelDarkYellow1
import com.ykseon.toastmaster.ui.theme.PastelPink2
import com.ykseon.toastmaster.ui.theme.PastelYellow1
import com.ykseon.toastmaster.ui.theme.darkColors
import com.ykseon.toastmaster.ui.theme.lightColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "TimerFragmentViewModel"
@HiltViewModel
class TimerFragmentViewModel @Inject constructor(
    val sharedState: SharedState
) : ViewModel() {
    fun startTimer(context: Context, role:String, name: String, cutoffs: String) {
        startActivity(
            context,
            Intent(context, TimerActivity::class.java).apply {
                putExtra("role", role)
                putExtra("name", name)
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

    init {
        sharedState.sortOption.onEach {
            database?.timerDAO()?.load()
        }.flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }
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
                dao.insert(Timer(role = it.role, name= it.name, cutoffs = it.cutoffs))
            }
        }

        loadTimer(context)
    }

    fun loadTimer(context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            initDatabase(context)
            val dao = database?.timerDAO()
            dao?.load()
        }
    }

    private fun String.toRoleOrder(): Int {
        return when(this) {
            SPEAKER_ROLE -> 1
            EVALUATOR_ROLE -> 2
            TABLE_TOPIC_ROLE -> 3
            DEBATE_ROLE -> 4
            else -> 5
        }
    }

    private suspend fun TimerDAO.load() {
        val list = mutableListOf<TimerRecordItem>()
        val sortedLit = when(sharedState.sortOption.value) {
            SortOption.ROLE -> getAllTimers().sortedBy { it.role.toRoleOrder() }
            SortOption.ALPHABETICAL-> getAllTimers().sortedBy { it.name }
            else -> getAllTimers().sortedBy { it.id }
        }

        sortedLit.mapTo(list) {
            TimerRecordItem(TimerItem(it.role, it.name, it.cutoffs), it.id)
        }

        list.add(TimerRecordItem(TimerItem("","",""), -1))
        _timerList.value = list
    }

    fun deleteTimer(id: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            database?.timerDAO()?.let { dao ->
                _timerList.value.firstOrNull { it.id == id }?.let {
                    dao.delete(Timer(it.id, it.item.role, it.item.name, it.item.cutoffs))
                }
                logItems(id)
                dao.load()
            }
        }
    }

    fun editTimer(id: Long) {
        val recordItem = _timerList.value.firstOrNull { it.id == id}
        val dialog = CustomTimerDialog(object: CustomTimerDialogCallback {
            override fun onYesButtonClick(item: TimerItem) {
                updateItem(id, item)
                viewModelScope.launch(Dispatchers.Default) {
                    database?.timerDAO()?.load()
                }
            }
        })
        dialog.initialTimerItem = recordItem?.item
        dialog.show(fragmentManager!!, "Timer Input Dialog")
    }


    private fun updateItem(id: Long, item: TimerItem) {
        viewModelScope.launch(Dispatchers.Default) {
            database?.timerDAO()?.let { dao ->
                dao.update(Timer(id, item.role, item.name, item.cutoffs))
                dao.load()
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
    fun createItem(item: TimerItem) {
        viewModelScope.launch(Dispatchers.IO) {
            database?.timerDAO()?.let { dao ->
                dao.insert(Timer(role = item.role, name= item.name, cutoffs = item.cutoffs))
                dao.load()
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
            override fun onYesButtonClick(item: TimerItem) {
                createItem(item)
            }
        })
        dialog.show(fragmentManager!!, "Timer Input Dialog")
    }

    fun getIconId(recordItem: TimerRecordItem): Int =
        when(recordItem.item.role) {
            SPEAKER_ROLE -> R.drawable.ic_speaker_foreground
            DEBATE_ROLE -> R.drawable.ic_debate_foreground
            EVALUATOR_ROLE -> R.drawable.ic_evaluator_foreground
            TABLE_TOPIC_ROLE -> R.drawable.ic_table_topic_foreground
            else -> R.drawable.ic_speaker_foreground
        }
}

