package com.ykseon.toastmaster.ui.preference

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ykseon.toastmaster.R
import com.ykseon.toastmaster.common.compose.Header
import com.ykseon.toastmaster.ui.theme.PastelBlue1
import com.ykseon.toastmaster.ui.theme.PastelBlue3
import com.ykseon.toastmaster.ui.theme.PastelDarkBlue3
import com.ykseon.toastmaster.ui.theme.TimerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@AndroidEntryPoint
class PreferencesActivity : AppCompatActivity() {

    private val preferencesViewModel by viewModels<PreferencesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val composeView = ComposeView(this)
        composeView.setContent {
            TimerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    PreferencesScreen(preferencesViewModel)
                }
            }
        }
        setContentView(composeView)
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun PreferencesScreen(viewModel: PreferencesViewModel) {
    val bufferTimes = stringArrayResource(id = R.array.buffer_times).toList()
    val greenCardPolicies = stringArrayResource(id = R.array.green_card_policies).toList()
    val sortTypes = stringArrayResource(id = R.array.sort_type).toList()
    val showTimerDetails = viewModel.showTimerDetails.collectAsState()
    val showRemainingTime = viewModel.showRemainingTime.collectAsState()
    val acceleration = viewModel.acceleration.collectAsState()
    val startTimerImmediate = viewModel.startTimerImmediate.collectAsState()
    val bufferTime = viewModel.bufferTime.collectAsState()
    val greenCardPolicy = viewModel.greenCardPolicy.collectAsState()
    val beepSound = viewModel.beepSound.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Header("Settings") { (context as? PreferencesActivity)?.finish() }
        }

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, bottom = 16.dp)
                .background(MaterialTheme.colors.background),
        ) {
            PreferenceSection("Timer card screen") {
                DropdownPreference(
                    title = "Sort",
                    items = sortTypes,
                    onItemSelected = { viewModel.setSortType(sortTypes.indexOf(it)) },
                    selectedString = sortTypes[viewModel.sortType.collectAsState().value]
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            PreferenceSection("Timer screen") {
                SwitchPreference(
                    title = "Show timer details",
                    checked = showTimerDetails.value,
                    onCheckedChange = { viewModel.setShowTimerDetails(it) }
                )
                SwitchPreference(
                    title = "Show remaining time ",
                    checked = showRemainingTime.value,
                    onCheckedChange = { viewModel.setShowRemainingTime(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceSection("Timer actions") {
                SwitchPreference(
                    title = "Start timer immediately",
                    checked = startTimerImmediate.value,
                    onCheckedChange = { viewModel.setStartTimerImmediate(it) }
                )

                DropdownPreference(
                    title = "Buffer time",
                    items = bufferTimes,
                    onItemSelected = { viewModel.setBufferTime(bufferTimes.indexOf(it)) },
                    selectedString = bufferTimes[bufferTime.value]
                )

                DropdownPreference(
                    title = "Green card policy",
                    items = greenCardPolicies,
                    onItemSelected = { viewModel.setGreenCardPolicy(greenCardPolicies.indexOf(it)) },
                    selectedString = greenCardPolicies[greenCardPolicy.value]
                )

                SwitchPreference(
                    title = "Beep sound when card changes",
                    checked = beepSound.value,
                    onCheckedChange = { viewModel.setBeepSound(it)}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceSection("Developer options") {
                SwitchPreference(
                    title = "Accelerate 10x speed",
                    checked = acceleration.value,
                    onCheckedChange = { viewModel.setAcceleration(it) }
                )
            }
        }
    }
}


@Composable
fun SwitchPreference(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    // Define the switch colors based on the switch state
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = if (isSystemInDarkTheme()) PastelBlue3 else PastelDarkBlue3,
        uncheckedThumbColor = Color.White,
        uncheckedTrackColor = if(isSystemInDarkTheme()) Color.LightGray else Color.DarkGray
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = { newCheckedState ->
                onCheckedChange(newCheckedState)
            },
            colors = switchColors
        )
    }
}

@Composable
fun PreferenceSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface, shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Divider(modifier = Modifier.height(1.dp), color = Color.Gray)
        content()
    }

}

@Composable
fun DropdownPreference(
    title: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    selectedString: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable {
                expanded = true
            }
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = title,
            color = MaterialTheme.colors.onSurface,
        )
        Box(modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedString,
                color = if (isSystemInDarkTheme()) PastelBlue1 else PastelDarkBlue3,
                fontSize = 15.sp
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colors.surface)
            ) {
                for ((index, item) in items.withIndex()) {
                    DropdownMenuItem(
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                    ) {
                        Text(
                            text = item,
                            color = MaterialTheme.colors.onSurface
                        )
                    }
                    if (index != items.lastIndex) {
                        Divider(
                            modifier = Modifier
                                .height(1.dp)
                                .padding(start = 16.dp, end = 16.dp),
                            color = Color.Gray
                        )
                    }

                }
            }
        }
    }
}
