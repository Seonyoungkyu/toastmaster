package com.ykseon.toastmaster.ui.report

import androidx.lifecycle.ViewModel
import com.ykseon.toastmaster.common.SharedState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val sharedState: SharedState
): ViewModel() {

    val records = sharedState.timeRecords.asStateFlow()
}