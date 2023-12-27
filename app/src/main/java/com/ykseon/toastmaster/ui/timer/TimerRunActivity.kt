package com.ykseon.toastmaster.ui.timer

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ykseon.toastmaster.R
import com.ykseon.toastmaster.common.beeper.Beeper
import com.ykseon.toastmaster.databinding.TimerActivityMainBinding
import com.ykseon.toastmaster.model.SettingsPreferences
import com.ykseon.toastmaster.ui.nameinput.NameInputDialog
import com.ykseon.toastmaster.ui.report.ReportActivity
import com.ykseon.toastmaster.ui.theme.TimerTheme
import com.ykseon.toastmaster.ui.theme.TransparentDarkGray
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class TimerRunActivity : AppCompatActivity() {

    private lateinit var binding: TimerActivityMainBinding
    private val viewModel by viewModels<TimerViewModel>()

    @Inject
    lateinit var settingsPreferences: SettingsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initTimerInfo(intent)

        val composeView = ComposeView(this)
        composeView.setContent {
            TimerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    TimerScreen(viewModel)
                }
            }
        }
        setContentView(composeView)

        if(savedInstanceState == null) viewModel.tryStart()

        hideStatusBar()
        keepScreenOn()
        setCloseTimer()
        setRecordToast()
        setNameInputDialog()
        setBeepSound()
    }

    private var beepSoundJob: Job? = null
    private fun setBeepSound() {
        viewModel.beepSound.onEach {
            if (it) {
                beepSoundJob = viewModel.currentTime.distinctUntilChanged { old, new ->
                    old.javaClass == new.javaClass
                }.filter {state->
                    state !is TimerState.Initialized && state !is TimerState.Ready
                }
                .onEach {
                    val resID = resources.getIdentifier(
                        "bell", "raw",
                        packageName
                    )
                    Beeper(this, resID).play()
                }.launchIn(lifecycleScope)
            }
            else {
                beepSoundJob?.cancel()
            }
        }.launchIn(lifecycleScope)
    }

    private fun setNameInputDialog() {
        viewModel.showNameInputDialog.onEach{
            NameInputDialog(this).show {
                viewModel.name = it
                viewModel.stopButtonClick()
            }
        }.launchIn(lifecycleScope)

    }
    private fun keepScreenOn() = window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    private fun hideStatusBar() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
    }
    private fun initTimerInfo(intent: Intent) {
        val role = checkNotNull(intent.getStringExtra("role"))
        val name = checkNotNull(intent.getStringExtra("name"))
        val cutoffs = checkNotNull(intent.getStringExtra("cutoffs"))

        viewModel.setRoleAndCutoffs(role, name, cutoffs)
    }
    private fun setCloseTimer() {
        viewModel.closeTimer.onEach {
            finish()

            ContextCompat.startActivity(
                this,
                Intent(this, ReportActivity::class.java),
                null
            )

        }.launchIn(lifecycleScope)
    }
    private fun setRecordToast() {
        viewModel.recordToastShow.onEach{ message ->
            Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
        }.launchIn(lifecycleScope)
    }

}

@Composable
fun TimerScreen(viewModel: TimerViewModel) {

    val progressValue by viewModel.progressValue.collectAsState()
    val timeText by viewModel.timeText.collectAsState()
    val timeTextColor by viewModel.timeTextColor.collectAsState()
    val backgroundColor by viewModel.backgroundColor.collectAsState()
    val remainingText by viewModel.remainingText.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val detailVisible by viewModel.detailVisible.collectAsState()

    val cutoffEnd = viewModel.cutOffTimes[3].toFloat()
    val grayRatio = viewModel.cutOffTimes[0].toFloat() / cutoffEnd
    val greenRatio = (viewModel.cutOffTimes[1] - viewModel.cutOffTimes[0]).toFloat() / cutoffEnd
    val yellowRatio = (viewModel.cutOffTimes[2] - viewModel.cutOffTimes[1]).toFloat() / cutoffEnd
    val redRatio = (viewModel.cutOffTimes[3] - viewModel.cutOffTimes[2]).toFloat() / cutoffEnd

    val startButtonState by remember {
        derivedStateOf {
            if (currentTime is TimerState.Initialized || currentTime.paused) R.drawable.ic_action_start
            else R.drawable.ic_action_pause
        }
    }

    ConstraintLayout (
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(backgroundColor))
            .clickable {
                viewModel.toggleDetailVisible()
            }
    ) {
        val (progress, info, time, button) = createRefs()

        Box(
            modifier = Modifier
                .constrainAs(progress) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(start = 30.dp, end = 30.dp, top = 30.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = detailVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            color = TransparentDarkGray,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                            .width(10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .padding(top = 3.dp, bottom = 3.dp, start = 20.dp, end = 20.dp)

                    ) {
                        ColorProgress(grayRatio, greenRatio, yellowRatio, redRatio) { progressValue }
                    }

                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                            .width(10.dp)
                    )

                    Text(text = viewModel.name, fontSize = 20.sp, color = Color.Yellow)
                    Text(text = viewModel.cutoffs, fontSize = 18.sp, color = Color.Yellow)

                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                            .width(10.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .constrainAs(time) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .clickable {
                    viewModel.toggleTimerRemaining()
                }
            ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = remainingText.toString(), textAlign = Center, color = Color(timeTextColor))
            TimeText(text = timeText, color = Color(timeTextColor))
        }

        Row ( modifier = Modifier
            .constrainAs(button) {
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .padding(bottom = 30.dp)
        ) {
            ControlButton(resourceId = startButtonState) {
                viewModel.startButtonClick()
            }

            Spacer(modifier = Modifier.width(100.dp))

            ControlButton(resourceId = R.drawable.ic_action_stop) {
                viewModel.stopButtonClick()
            }

        }
    }

}

@Composable
fun TimerInfoToken(text: String) {
    Box {
        Text(
            modifier = Modifier
                .background(
                    color = Color.Gray,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(4.dp),
            text = text,
            color = Color.White
        )
    }
}

@Composable
fun TimeText(text: String, color: Color)  {
    Text( text = text, fontSize = 60.sp, color = color)
}

@Composable
fun ControlButton(resourceId: Int, onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        shape = CircleShape,
        modifier = Modifier
            .size(70.dp)
            ,
        contentPadding = PaddingValues(all = 0.dp),
        colors = ButtonDefaults.buttonColors (
            backgroundColor = Color.Gray,
            contentColor = Color.Gray
        )
    ) {
        Icon(painter = painterResource(id = resourceId),
            contentDescription = null, tint = Color.White
        )
    }
}

@Composable
fun ColorProgress(grey: Float, green: Float, yellow: Float, red: Float, progress: () -> Float) {

    var progressWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current.density

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .onPlaced { coordinates ->
                    progressWidth = (coordinates.size.width / density).dp
                }
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                    .background(Color.DarkGray)
                    .fillMaxHeight()
                    .weight(grey)
            )
            Box(
                modifier = Modifier
                    .background(Color.Green)
                    .fillMaxHeight()
                    .weight(green)
            )
            Box(
                modifier = Modifier
                    .background(Color.Yellow)
                    .fillMaxHeight()
                    .weight(yellow)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                    .background(Color.Red)
                    .fillMaxHeight()
                    .weight(red)
            )
        }
    }

    LinearProgressIndicator(
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = 6.dp,
                    bottomStart = 6.dp,
                    topEnd = 6.dp, // if (progress() >= 1.0F) 6.dp else 0.dp,
                    bottomEnd = 6.dp // if (progress() >= 1.0F) 6.dp else 0.dp,
                )
            )
            .background(Color.Transparent)
            .fillMaxHeight()
            .fillMaxWidth(),
        color = Color.Blue,
        progress = progress()
    )
}