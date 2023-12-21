package com.ykseon.toastmaster.ui.report

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ykseon.toastmaster.R
import com.ykseon.toastmaster.common.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val sharedState: SharedState
): ViewModel() {

    val records = sharedState.timeRecords.asStateFlow()
    val showConfirmDelete = MutableSharedFlow<Unit>()
    fun deleteAll() {
        viewModelScope.launch { showConfirmDelete.emit(Unit) }
    }

    fun deleteTimeRecords() {
        sharedState.timeRecords.value = mutableListOf()
    }
}