package com.ykseon.toastmaster.ui.timer

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ykseon.toastmaster.R
import com.ykseon.toastmaster.common.CREATION_SYSTEM_ROLE
import com.ykseon.toastmaster.ui.contextmenu.ContextMenuItem
import com.ykseon.toastmaster.ui.contextmenu.ContextMenuPopup
import com.ykseon.toastmaster.ui.theme.TimerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val TAG = "TimerFragment"
@AndroidEntryPoint
class TimerFragment : Fragment() {

    private val timerFragmentViewModel by viewModels<TimerFragmentViewModel>()
    private var dialog: CustomTimerDialog? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val composeView = ComposeView(requireContext())
        composeView.setContent {
            TimerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    TimerGridView(timerFragmentViewModel)
                }
            }
        }
        return composeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        timerFragmentViewModel.loadTimer(view.context)

        timerFragmentViewModel.showCustomTimerDialog.onEach { info ->
            activity?.let {activity ->
                dialog = CustomTimerDialog(object : CustomTimerDialogCallback {
                    override fun onYesButtonClick(item: TimerItem) {
                        if (info == null) {
                            timerFragmentViewModel.createItem(item)
                        }else {
                            timerFragmentViewModel.updateItemAndReload(info.id , item)
                        }

                    }
                })

                info?.let { dialog?.initialTimerItem = it.timerItem }
                dialog?.show(activity.supportFragmentManager, "Timer Input Dialog")
            }
        }.launchIn(lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun TimerGridView(viewModel: TimerFragmentViewModel) {

    val timerItems = viewModel.timerList.collectAsState()
    // 그리드뷰 생성
    LazyVerticalGrid(
        columns = GridCells.Adaptive(100.dp),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(timerItems.value) { recordItem ->
            Log.i(TAG,"TimerList - addView ($recordItem.id)")
            if (recordItem.id != -1L) {
                TimerCard(viewModel, recordItem)
            } else {
                TimerCreationCard(viewModel)
            }
        }

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimerCreationCard(viewModel: TimerFragmentViewModel) {
    Card (
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = viewModel.getBgColor(CREATION_SYSTEM_ROLE, isSystemInDarkTheme()),
        onClick = { viewModel.createTimer() },
    ) {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            text = "+",
            style = TextStyle(
                fontWeight = FontWeight.Normal,
                fontSize = 45.sp,
                color = MaterialTheme.colors.onSurface
            ),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun TimerCard(viewModel: TimerFragmentViewModel, recordItem: TimerRecordItem) {
    val context = LocalView.current.context
    var globalOffset by remember { mutableStateOf(Offset(0F, 0F))}
    var longPressCount by remember { mutableIntStateOf(0) }
    var record by remember {mutableStateOf(TimerRecordItem(TimerItem("","", "")))}
    Log.i(TAG,"TimerList - TimerCard id(${recordItem.id}) hash(${recordItem.hashCode()})")

    record = recordItem

    Card(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .onGloballyPositioned {
                globalOffset = it.positionInWindow()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        longPressCount += 1
                        val rawX = (offset.x + globalOffset.x)
                        val rawY = (offset.y + globalOffset.y)
                        Log.i(
                            TAG,
                            "TimerList - LongPress (${record.id}) hash(${record.hashCode()})"
                        )
                        ContextMenuPopup(
                            context = context,
                            items = arrayListOf(
                                ContextMenuItem(
                                    context.resources.getString(R.string.context_menu_delete),
                                ) { popup, obj ->
                                    val id = (obj as Long)
                                    viewModel.deleteTimer(id)
                                    popup.dismiss()
                                },
                                ContextMenuItem(
                                    context.resources.getString(R.string.context_menu_edit),
                                ) { popup, obj ->
                                    val id = (obj as Long)
                                    viewModel.editTimer(id)
                                    popup.dismiss()
                                }
                            ),
                            record.id
                        ).show(rawX.toInt(), rawY.toInt())
                    },
                    onTap = {
                        Log.i(TAG, "TimerList - Press (${record.id}) hash(${record.hashCode()})")
                        viewModel.startTimer(context, record.item.role, record.item.name, record.item.cutoffs)
                    }
                )
            },
        shape = RoundedCornerShape(8.dp),
        backgroundColor = viewModel.getBgColor(
            record.item.role, isSystemInDarkTheme(),
            record.item.cutoffs.replace("-","").toInt()
        ),
    ) {
        Box(
            modifier = Modifier.wrapContentSize(align = Alignment.TopStart)
        ) {
            Icon(
                modifier = Modifier.requiredSize(30.dp),
                painter = painterResource(id = viewModel.getIconId(recordItem)),
                contentDescription = "speaker icon",
            )
        }

        Column(
            modifier = Modifier
                .wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            Text(
                text = record.item.name,
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = MaterialTheme.typography.body1.fontSize,
                    color = MaterialTheme.colors.onSurface
                ),
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = record.item.cutoffs,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.body2.fontSize,
                    color = MaterialTheme.colors.onSurface
                )
            )
        }
    }
}

@Preview
@Composable
fun MainContent() {
    Card(modifier = Modifier.size(120.dp)) {
        
        Box(
            modifier = Modifier.wrapContentSize(align = Alignment.TopStart)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_speaker_foreground),
                modifier = Modifier.requiredSize(30.dp),
                contentDescription = "speaker icon",
            )
        }
        Column(
            modifier = Modifier
                .wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            Text(
                text = "aa",
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = MaterialTheme.typography.body1.fontSize,
                    color = MaterialTheme.colors.onSurface
                )
            )

            Text(
                text = "BB",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.body2.fontSize,
                    color = MaterialTheme.colors.onSurface
                )
            )
        }
    }
}
