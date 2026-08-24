package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PRESET_SPEEDS
import com.example.model.SpeedStatus
import com.example.model.SpeedUiState
import com.example.ui.theme.MinimalBlue
import com.example.ui.theme.MinimalBlueDark
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalSurfaceLow
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusActiveGreenBg
import com.example.ui.theme.StatusActiveGreenBorder
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusErrorRedBg
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.StatusWarningAmberBg

@Composable
fun SpeedControlPanel(
    uiState: SpeedUiState,
    onSpeedSelected: (Double) -> Unit,
    onRefreshDiagnostics: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomSlider by remember { mutableStateOf(false) }

    val isActuallyActive = uiState.status == SpeedStatus.ACTIVE &&
            Math.abs(uiState.actualSpeed - uiState.requestedSpeed) < 0.01

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                spotColor = Color(0x1F000000)
            )
            .testTag("speed_control_panel"),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Top Section: Output summary & Target
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Current Output
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "CURRENT OUTPUT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MinimalTextMuted,
                        letterSpacing = 1.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${formatSpeedNumber(if (isActuallyActive) uiState.actualSpeed else uiState.requestedSpeed)}x",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = MinimalBlueDark,
                            letterSpacing = (-0.5).sp
                        )

                        // Status Pill Badge
                        val badgeBg = when {
                            isActuallyActive -> StatusActiveGreenBg
                            uiState.status == SpeedStatus.CROSS_ORIGIN_IFRAME -> StatusErrorRedBg
                            uiState.status == SpeedStatus.NO_VIDEO -> Color(0xFFF1F5F9)
                            else -> StatusWarningAmberBg
                        }
                        val badgeText = when {
                            isActuallyActive -> StatusActiveGreen
                            uiState.status == SpeedStatus.CROSS_ORIGIN_IFRAME -> StatusErrorRed
                            uiState.status == SpeedStatus.NO_VIDEO -> MinimalTextSecondary
                            else -> StatusWarningAmber
                        }
                        val badgeBorder = when {
                            isActuallyActive -> StatusActiveGreenBorder
                            uiState.status == SpeedStatus.CROSS_ORIGIN_IFRAME -> Color(0xFFFECACA)
                            uiState.status == SpeedStatus.NO_VIDEO -> Color(0xFFE2E8F0)
                            else -> Color(0xFFFDE68A)
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = badgeBg,
                            border = BorderStroke(1.dp, badgeBorder),
                            modifier = Modifier.testTag("active_speed_badge")
                        ) {
                            Text(
                                text = when {
                                    isActuallyActive -> "ACTIVE"
                                    uiState.status == SpeedStatus.NO_VIDEO -> "NO VIDEO"
                                    uiState.status == SpeedStatus.CROSS_ORIGIN_IFRAME -> "IFRAME"
                                    else -> "SYNCING"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = badgeText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Right: Target & Tools
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Target",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MinimalTextMuted
                        )
                        Text(
                            text = "rate: ${formatSpeedNumber(uiState.requestedSpeed)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MinimalTextPrimary
                        )
                    }

                    IconButton(
                        onClick = { showCustomSlider = !showCustomSlider },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("toggle_custom_slider_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Slider Settings",
                            tint = if (showCustomSlider) MinimalBlue else MinimalTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onRefreshDiagnostics,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("refresh_diagnostics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh video detection",
                            tint = MinimalTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Custom Slider Section (collapsible)
            AnimatedVisibility(visible = showCustomSlider) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Continuous Speed Slider",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MinimalTextSecondary
                        )
                        Text(
                            text = "${formatSpeedNumber(uiState.requestedSpeed)}x",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MinimalBlue
                        )
                    }
                    Slider(
                        value = uiState.requestedSpeed.toFloat(),
                        onValueChange = { newSpeed ->
                            val rounded = (Math.round(newSpeed * 4.0) / 4.0)
                            onSpeedSelected(rounded)
                        },
                        valueRange = 0.5f..16.0f,
                        steps = 61,
                        colors = SliderDefaults.colors(
                            thumbColor = MinimalBlue,
                            activeTrackColor = MinimalBlue,
                            inactiveTrackColor = MinimalBorder
                        ),
                        modifier = Modifier.testTag("speed_custom_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid of Speed Buttons
            // Row 1: 1x, 1.25x, 1.5x, 1.75x
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpeedButton(1.0, uiState.requestedSpeed, onSpeedSelected, Modifier.weight(1f))
                SpeedButton(1.25, uiState.requestedSpeed, onSpeedSelected, Modifier.weight(1f))
                SpeedButton(1.5, uiState.requestedSpeed, onSpeedSelected, Modifier.weight(1f))
                SpeedButton(1.75, uiState.requestedSpeed, onSpeedSelected, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: 2x, 2.5x, 3x, 4x
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpeedButton(2.0, uiState.requestedSpeed, onSpeedSelected, Modifier.weight(1f))
                SpeedButton(2.5, uiState.requestedSpeed, onSpeedSelected, Modifier.weight(1f))
                SpeedButton(3.0, uiState.requestedSpeed, onSpeedSelected, Modifier.weight(1f))
                SpeedButton(4.0, uiState.requestedSpeed, onSpeedSelected, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 3: 5x, 7.5x, 10x Speed (Prominent)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SpeedButton(5.0, uiState.requestedSpeed, onSpeedSelected, Modifier.weight(1f))
                SpeedButton(7.5, uiState.requestedSpeed, onSpeedSelected, Modifier.weight(1f))

                // Prominent 10x Button (spanning 2 column weights)
                val is10xSelected = Math.abs(uiState.requestedSpeed - 10.0) < 0.01
                Button(
                    onClick = { onSpeedSelected(10.0) },
                    modifier = Modifier
                        .weight(2f)
                        .height(44.dp)
                        .testTag("quick_10x_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (is10xSelected) MinimalBlue else MinimalBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "10x Speed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Minimalist Debug Bridge Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenDiagnostics() }
                    .testTag("open_diagnostics_button"),
                shape = RoundedCornerShape(16.dp),
                color = MinimalSurfaceLow,
                border = BorderStroke(1.dp, MinimalBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DEBUG BRIDGE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MinimalTextMuted,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = if (uiState.diagnostics.videoFound) "[VIDEO DETECTED]" else "[READY]",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.diagnostics.videoFound) MinimalBlue else MinimalTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "URL: ${if (uiState.currentUrl.length > 40) uiState.currentUrl.take(38) + "..." else uiState.currentUrl}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MinimalTextSecondary
                    )

                    Text(
                        text = "Found: ${if (uiState.diagnostics.videoFound) "YES (HTML5 Video Element)" else "NO (Searching DOM...)"}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MinimalTextSecondary
                    )

                    Text(
                        text = "Result: { success: ${isActuallyActive}, actual: ${formatSpeedNumber(uiState.actualSpeed)} }",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MinimalTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedButton(
    speed: Double,
    currentSpeed: Double,
    onSpeedSelected: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = Math.abs(currentSpeed - speed) < 0.01
    val label = formatSpeedNumber(speed) + "x"

    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSpeedSelected(speed) }
            .testTag("speed_chip_${label.replace(".", "_")}"),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MinimalBlue else Color(0xFFF1F5F9),
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) Color.White else MinimalTextPrimary
            )
        }
    }
}

private fun formatSpeedNumber(speed: Double): String {
    return if (speed % 1.0 == 0.0) {
        "${speed.toInt()}"
    } else {
        "$speed"
    }
}
