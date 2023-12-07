package com.ykseon.toastmaster.ui.timer

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TimerFragmentViewModel @Inject constructor() : ViewModel() {
    fun startTimer(view: View, role:String, cutoffs: String) {
        startActivity(
            view.context,
            Intent(view.context, TimerActivity::class.java).apply {
                putExtra("role", role)
                putExtra("cutoffs", cutoffs)
            },
            null
        )
    }
}