package com.ykseon.toastmaster.ui.timer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat.getInsetsController
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.ykseon.toastmaster.common.ANONYMOUS
import com.ykseon.toastmaster.databinding.TimerActivityMainBinding
import com.ykseon.toastmaster.ui.nameinput.NameInputDialog
import com.ykseon.toastmaster.ui.report.ReportActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class TimerActivity : AppCompatActivity() {

    private lateinit var binding: TimerActivityMainBinding
    private val timerViewModel by viewModels<TimerViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = TimerActivityMainBinding.inflate(layoutInflater)
        binding.viewModel = timerViewModel.apply {
            defaultBackgroundColor = getDefaultBackgroundColor()
        }
        binding.lifecycleOwner = this
        setContentView(binding.root)

        val windowInsetsController = getInsetsController(window,window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())

        val role = checkNotNull(intent.getStringExtra("role"))
        val name = checkNotNull(intent.getStringExtra("name"))
        val cutoffs = checkNotNull(intent.getStringExtra("cutoffs"))

        timerViewModel.setRoleAndCutoffs(role, name, cutoffs)
        if(savedInstanceState == null) {
            timerViewModel.tryStart()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        binding.root.doOnPreDraw {
            timerViewModel.animIconMovingSpan = binding.root.width
        }

        binding.timerMain.setOnClickListener {
            timerViewModel.setDetailVisible(binding.headerInfo.visibility != View.VISIBLE)
        }

        binding.headerInfo.setOnClickListener {
            timerViewModel.setDetailVisible(binding.headerInfo.visibility != View.VISIBLE)
        }


        binding.timeText.setOnClickListener {
            timerViewModel.toggleTimerRemaining()
        }

        timerViewModel.detailVisible.onEach { visible ->
            binding.headerInfo.animate()
                .alpha(if (!visible) 0f else 1f)
                .setDuration(300)
                .withEndAction {
                    binding.headerInfo.visibility = if (visible) View.VISIBLE else View.INVISIBLE
                }
                .start()
        }.launchIn(lifecycleScope)

        timerViewModel.showNameInputDialog.onEach{
            NameInputDialog(this).show {
                timerViewModel.name = it
                timerViewModel.stopButtonClick()
            }
        }.launchIn(lifecycleScope)

        timerViewModel.closeTimer.onEach {
            finish()

            ContextCompat.startActivity(
                this,
                Intent(this, ReportActivity::class.java),
                null
            )

        }.launchIn(lifecycleScope)
        moveAnimationIcon()
        showRecordToast()
    }

    private fun showRecordToast() {
        timerViewModel.recordToastShow.onEach{message ->
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
        }.launchIn(lifecycleScope)
    }

    private fun moveAnimationIcon() {

        timerViewModel.animationTranslation.onEach { x ->
            binding.lottie.let {
                it.updateLayoutParams<FrameLayout.LayoutParams> {
                    leftMargin = x
                }
            }
        }.launchIn(lifecycleScope)
    }
    private fun getDefaultBackgroundColor(): Int {
        val typedValue = TypedValue()
        val result = theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
        return if (result) typedValue.data
        else Color.LTGRAY
    }
}
