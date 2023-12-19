package com.ykseon.toastmaster.ui.report

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ykseon.toastmaster.common.compose.Header
import com.ykseon.toastmaster.ui.theme.PastelBlue1
import com.ykseon.toastmaster.ui.theme.PastelDarkBlue2
import com.ykseon.toastmaster.ui.theme.TimerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReportActivity : AppCompatActivity() {

    private val recordViewModel by viewModels<ReportViewModel>()

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
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun TimerRecordView(viewModel: ReportViewModel) {

    val recordItems = viewModel.records.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Header("Timer's report") { (context as? ReportActivity)?.finish() }
        }
    ) {
        Column {
            Spacer(modifier = Modifier.height(6.dp))
            LazyColumn {
                items(recordItems.value) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                            .height(40.dp)
                            .background(
                                color = MaterialTheme.colors.surface,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name,
                            style = TextStyle(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(6f).padding(start = 8.dp)
                        )
                        Text(
                            text = item.role,
                            style = TextStyle(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(3f)
                        )
                        Text(
                            text = item.time,
                            style = TextStyle(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = if (item.qualified) "Qualified" else "Not qualified",
                            color = if (item.qualified) {
                                if (isSystemInDarkTheme()) PastelBlue1 else PastelDarkBlue2
                            } else {
                                Color.Red
                            },
                            style = TextStyle(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(3f).padding(end = 8.dp)
                        )
                    }

                }
            }
        }
    }
}