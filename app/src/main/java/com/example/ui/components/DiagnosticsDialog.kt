package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.SpeedUiState
import com.example.ui.theme.MinimalBlue
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalSurfaceLow
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusWarningAmber

@Composable
fun DiagnosticsDialog(
    uiState: SpeedUiState,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onTestSpeed: (Double) -> Unit
) {
    val diag = uiState.diagnostics
    val isMatched = diag.videoFound && Math.abs(diag.actualPlaybackRate - diag.requestedSpeed) < 0.01

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 680.dp)
                .testTag("diagnostics_dialog"),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 0.dp,
            shadowElevation = 16.dp,
            color = Color.White,
            border = BorderStroke(1.dp, MinimalBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Developer Diagnostics",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MinimalTextPrimary
                        )
                        Text(
                            text = "DIKSHA JavaScript Engine",
                            fontSize = 11.sp,
                            color = MinimalTextSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_diagnostics_button")
                    ) {
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

                // Diagnostic Parameters Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MinimalSurfaceLow,
                    border = BorderStroke(1.dp, MinimalBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DiagnosticRow(
                            label = "WebView URL:",
                            value = if (diag.url.length > 36) "${diag.url.take(34)}..." else diag.url,
                            valueFontWeight = FontWeight.Normal
                        )

                        DiagnosticRow(
                            label = "Video found:",
                            value = if (diag.videoFound) "YES" else "NO",
                            valueColor = if (diag.videoFound) StatusActiveGreen else StatusErrorRed
                        )

                        DiagnosticRow(
                            label = "Number of videos:",
                            value = "${diag.videoCount}"
                        )

                        DiagnosticRow(
                            label = "Active video:",
                            value = if (diag.activeVideo) "YES" else "NO",
                            valueColor = if (diag.activeVideo) StatusActiveGreen else StatusErrorRed
                        )

                        DiagnosticRow(
                            label = "Requested speed:",
                            value = formatSpeed(diag.requestedSpeed),
                            valueColor = MinimalBlue
                        )

                        DiagnosticRow(
                            label = "Actual playbackRate:",
                            value = formatSpeed(diag.actualPlaybackRate),
                            valueColor = if (isMatched) StatusActiveGreen else StatusWarningAmber
                        )

                        if (diag.isCrossOriginIframe) {
                            DiagnosticRow(
                                label = "Iframe Security:",
                                value = "CROSS-ORIGIN DETECTED",
                                valueColor = StatusErrorRed
                            )
                        }

                        DiagnosticRow(
                            label = "Status:",
                            value = if (isMatched) "SUCCESS" else diag.status,
                            valueColor = if (isMatched) StatusActiveGreen else if (diag.isCrossOriginIframe) StatusErrorRed else StatusWarningAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Verification Test Actions
                Text(
                    text = "SPEED VERIFICATION TESTS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MinimalTextMuted,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onTestSpeed(10.0) },
                        colors = ButtonDefaults.buttonColors(containerColor = MinimalBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("test_10x_speed_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("10x Test", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { onTestSpeed(5.0) },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MinimalBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_5x_speed_button")
                    ) {
                        Text("5x", color = MinimalTextPrimary, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { onTestSpeed(1.0) },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MinimalBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_1x_speed_button")
                    ) {
                        Text("1x", color = MinimalTextPrimary, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Refresh Button
                OutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diagnostics_refresh_action"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MinimalBorder)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = MinimalBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Re-scan DOM & Refresh Diagnostics", color = MinimalBlue, fontSize = 12.sp)
                }

                // Recent JS Bridge Event Logs
                if (uiState.logMessages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "RECENT BRIDGE EVENTS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = MinimalTextMuted,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(8.dp)
                    ) {
                        LazyColumn {
                            items(uiState.logMessages) { logMsg ->
                                Text(
                                    text = logMsg,
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String,
    valueColor: Color = MinimalTextPrimary,
    valueFontWeight: FontWeight = FontWeight.Bold
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MinimalTextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = valueColor,
            fontWeight = valueFontWeight,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun formatSpeed(speed: Double): String {
    return if (speed % 1.0 == 0.0) {
        "${speed.toInt()}x"
    } else {
        "${speed}x"
    }
}
