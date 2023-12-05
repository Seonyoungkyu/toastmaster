package com.ykseon.toastmaster.ui.timer

import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat.getInsetsController
import androidx.core.view.WindowInsetsCompat
import com.ykseon.toastmaster.R
import com.ykseon.toastmaster.databinding.TimerActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TimerActivity : AppCompatActivity() {

    private lateinit var binding: TimerActivityMainBinding
    private val timerViewModel by viewModels<TimerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = TimerActivityMainBinding.inflate(layoutInflater)
        binding.viewModel = timerViewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)
        // 시스템 UI를 숨깁니다.
        val windowInsetsController = getInsetsController(window,window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())

        val role = checkNotNull(intent.getStringExtra("role"))
        val cutoffs = checkNotNull(intent.getStringExtra("cutoffs"))

        timerViewModel.setRoleAndCutoffs(role, cutoffs)
        timerViewModel.startButtonClick()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

}
