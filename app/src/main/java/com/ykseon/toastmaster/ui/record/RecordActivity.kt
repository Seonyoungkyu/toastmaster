package com.ykseon.toastmaster.ui.record

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ykseon.toastmaster.model.TimeRecord
import com.ykseon.toastmaster.ui.theme.PastelBlue1
import com.ykseon.toastmaster.ui.theme.PastelDarkBlue2
import com.ykseon.toastmaster.ui.theme.TimerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordActivity : AppCompatActivity() {

    private val recordViewModel by viewModels<RecordViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val composeView = ComposeView(this)
        composeView.setContent {
            TimerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    TimerRecordView(recordViewModel)
                }
            }
        }
        setContentView(composeView)
    }
}
@Composable
fun TimerRecordView(viewModel: RecordViewModel) {

    val recordItems = viewModel.records.collectAsState()
    
    LazyColumn {
        items(recordItems.value) {item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .wrapContentSize()
            ) {
                Text(text = item.name,
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(3f)
                )
                Text(text = item.role,
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(3f)
                )
                Text(text = item.time,
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(2f)
                )
                Text(text = if (item.qualified) "Qualified" else "Unqualified",
                    color = if (item.qualified) {
                        if (isSystemInDarkTheme()) PastelBlue1 else PastelDarkBlue2
                    } else {
                        Color.Red
                    },
                    style = TextStyle(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(3f)
                )
            }

        }
    }
}