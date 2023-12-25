package com.ykseon.toastmaster.ui.timer

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ykseon.toastmaster.R
import com.ykseon.toastmaster.common.ANONYMOUS
import com.ykseon.toastmaster.common.CREATION_SYSTEM_ROLE
import com.ykseon.toastmaster.common.compose.ContextMenu
import com.ykseon.toastmaster.common.compose.ContextMenuItem
import com.ykseon.toastmaster.common.compose.ContextMenuState
import com.ykseon.toastmaster.common.compose.SizeTrackingBox
import com.ykseon.toastmaster.common.isInsideBounds
import com.ykseon.toastmaster.ui.theme.TimerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val TAG = "TimerFragment"
@AndroidEntryPoint
class TimerCardsFragment : Fragment() {

    private val viewModel by viewModels<TimerCardsViewModel>()
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
                    TimerCardContentView(viewModel)
                }
            }
        }
        return composeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadTimer(view.context)

        viewModel.showCustomTimerDialog.onEach { info ->
            activity?.let {activity ->
                dialog = CustomTimerDialog(object : CustomTimerDialogCallback {
                    override fun onYesButtonClick(item: TimerItem) {
                        if (info == null) {
                            viewModel.createItem(item)
                        }else {
                            if (info.duplicate) {
                                viewModel.createItem(item)
                            }
                            else {
                                viewModel.updateItemAndReload(info.id, item)
                            }
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

@OptIn(ExperimentalComposeUiApi::class)
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun TimerCardContentView(viewModel: TimerCardsViewModel) {
    var contextMenuState by remember {
        mutableStateOf( ContextMenuState(false,0,0, -1, listOf()) )
    }
    var contextMenuBound by remember { mutableStateOf( Rect(0F,0F,0F,0F)) }
    var rootSize by remember { mutableStateOf(Size(0F,0F))}

    val timerItems = viewModel.timerList.collectAsState()

    SizeTrackingBox(
        modifier = Modifier
            .pointerInteropFilter {
                // 터치 다운 이벤트 처리
                if (it.action == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "detectTap - ${it.x}, ${it.y}")
                    if (contextMenuState.show && !Offset(
                            it.x,
                            it.y
                        ).isInsideBounds(contextMenuBound)
                    ) {
                        contextMenuState = ContextMenuState(false, 0, 0, -1, listOf())
                        return@pointerInteropFilter true
                    }
                    return@pointerInteropFilter false
                }
                false // 이벤트 전파를 막음
            }
        ,onSizeChanged = { width, height -> rootSize = Size(width, height)}
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(100.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(8.dp)

        ) {
            items(timerItems.value) { recordItem ->
                Log.i(TAG, "TimerList - addView ($recordItem.id)")
                if (recordItem.id != -1L) {
                    TimerCard(
                        viewModel,
                        recordItem
                    ) { x, y, id, items ->
                        contextMenuState = ContextMenuState(true, x, y, id, items)
                    }
                } else {
                    TimerCreationCard(viewModel)
                }
            }

        }

        if (contextMenuState.show) {
            ContextMenu(
                touchX = contextMenuState.x,
                touchY = contextMenuState.y,
                parentSize = rootSize,
                id = contextMenuState.id,
                items = contextMenuState.items,
                onUpdateMenuBound = { rect -> contextMenuBound = rect.copy()}
            ) {
                contextMenuState = ContextMenuState(false,0,0, -1, listOf())
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimerCreationCard(viewModel: TimerCardsViewModel) {
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
                color = Color.LightGray // MaterialTheme.colors.onSurface
            ),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun TimerCard(
    viewModel: TimerCardsViewModel,
    recordItem: TimerRecordItem,
    onContextMenu: (Int, Int, Long, List<ContextMenuItem>) -> Unit
) {
    val context = LocalView.current.context
    var globalOffset by remember { mutableStateOf(Offset(0F, 0F))}
    var rootOffset by remember { mutableStateOf(Offset(0F, 0F))}
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
                rootOffset = it.positionInRoot()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        onContextMenu(
                            (offset.x + rootOffset.x).toInt(),
                            (offset.y + rootOffset.y).toInt(),
                            record.id,
                            arrayListOf(
                                ContextMenuItem(
                                    context.resources.getString(R.string.context_menu_delete),
                                ) { id ->
                                    viewModel.deleteTimer(id)
                                },
                                ContextMenuItem(
                                    context.resources.getString(R.string.context_menu_edit),
                                ) { id ->
                                    viewModel.editTimer(id)
                                },
                                ContextMenuItem(
                                    context.resources.getString(R.string.context_menu_duplicate),
                                ) { id ->
                                    viewModel.editTimer(id, true)
                                }
                            )
                        )
                    },
                    onTap = {
                        Log.i(TAG, "TimerList - Press (${record.id}) hash(${record.hashCode()})")
                        viewModel.startTimer(
                            context,
                            record.item.role,
                            record.item.name,
                            record.item.cutoffs
                        )
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
            Row (verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.requiredSize(30.dp),
                    painter = painterResource(id = viewModel.getIconId(recordItem)),
                    contentDescription = "speaker icon",
                    tint = Color.White
                )
                Text(
                    text = record.item.role,
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        color = Color.White // MaterialTheme.colors.onSurface
                    ),
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(
            modifier = Modifier
                .wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            if (record.item.name != ANONYMOUS) {
                Text(
                    text = record.item.name,
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = MaterialTheme.typography.body1.fontSize,
                        color = Color.White // MaterialTheme.colors.onSurface
                    ),
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = record.item.cutoffs,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.body2.fontSize,
                        color = Color.White // MaterialTheme.colors.onSurface
                    )
                )
            } else {
                Text(
                    text = record.item.cutoffs,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        color = Color.White // MaterialTheme.colors.onSurface
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun MainContent() {

    Box (
        modifier = Modifier
            .background(
                color = colorResource(id = R.color.translucent_gray),
                shape = RoundedCornerShape(6.dp)
            )
    ) {
        Column(modifier = Modifier
            .padding(0.dp)
            .wrapContentSize(),
        ) {

            val array = arrayListOf(
                "Delete",
                "Edit",
                "Duplicate"
            )

            for (item in array) {
                Box (modifier = Modifier.padding(20.dp).widthIn(min = 100.dp)) {
                    Text(
                        modifier = Modifier.padding(8.dp).wrapContentSize(),
                        text = item,
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = MaterialTheme.typography.button.fontSize,
                        ),
                        color = Color.White
                    )
                }
            }
        }
    }
}
