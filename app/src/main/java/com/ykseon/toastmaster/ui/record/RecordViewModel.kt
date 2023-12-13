package com.ykseon.toastmaster.ui.record

import androidx.lifecycle.ViewModel
import com.ykseon.toastmaster.common.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val sharedState: SharedState
): ViewModel() {

    val records = sharedState.timeRecords.asStateFlow()
}