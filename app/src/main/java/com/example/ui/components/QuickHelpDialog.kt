package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.MinimalBlue
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary

@Composable
fun QuickHelpDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("quick_help_dialog"),
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 16.dp,
            color = Color.White,
            border = BorderStroke(1.dp, MinimalBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFD1E4FF),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = MinimalBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "How to Use & Test 10x",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalTextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MinimalTextSecondary
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MinimalBorder
                )

                StepItem(
                    stepNum = "1",
                    title = "Login to DIKSHA",
                    desc = "Navigate and log in with your normal DIKSHA credentials in the embedded WebView. Sessions and cookies are preserved."
                )

                StepItem(
                    stepNum = "2",
                    title = "Open Course Video",
                    desc = "Open any enrolled course module and start playing the HTML5 video lesson."
                )

                StepItem(
                    stepNum = "3",
                    title = "Press Speed (e.g. 10x)",
                    desc = "Tap '10x Speed' or any preset speed chip (1x to 10x) on the bottom control panel."
                )

                StepItem(
                    stepNum = "4",
                    title = "Verify ACTIVE Status",
                    desc = "The output badge updates to '10.0x ACTIVE' once video.playbackRate is verified at the requested rate."
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MinimalBlue)
                ) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    stepNum: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNum,
                    fontWeight = FontWeight.Bold,
                    color = MinimalBlue,
                    fontSize = 13.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MinimalTextPrimary
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = MinimalTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
