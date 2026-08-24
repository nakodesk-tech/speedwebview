package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
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
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showCustomSlider by remember { mutableStateOf(false) }

    val isActuallyActive = uiState.status == SpeedStatus.ACTIVE &&
            Math.abs(uiState.actualSpeed - uiState.requestedSpeed) < 0.01

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                spotColor = Color(0x1F000000)
            )
            .testTag("speed_control_panel"),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MinimalBorder.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Slider (expanded only when toggled)
            AnimatedVisibility(visible = showCustomSlider) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Custom Continuous Speed",
                            fontSize = 11.sp,
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
                    HorizontalDivider(color = MinimalBorder.copy(alpha = 0.5f))
                }
            }

            // Compact Main Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Dropdown Menu for Speed Selection
                Box {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, MinimalBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showDropdownMenu = true }
                            .testTag("speed_dropdown_trigger")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Speed Menu",
                                tint = MinimalBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Speed: ${formatSpeedNumber(uiState.requestedSpeed)}x",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MinimalBlueDark
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Open Dropdown",
                                tint = MinimalTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Dropdown menu showing all speed presets
                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false },
                        modifier = Modifier
                            .background(Color.White)
                            .testTag("speed_dropdown_menu")
                    ) {
                        Text(
                            text = "SELECT PLAYBACK SPEED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = MinimalTextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            letterSpacing = 0.8.sp
                        )

                        PRESET_SPEEDS.forEach { speed ->
                            val isSelected = Math.abs(uiState.requestedSpeed - speed) < 0.01
                            val label = "${formatSpeedNumber(speed)}x"

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (speed == 10.0) "10x (Ultra Fast)" else label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MinimalBlue else MinimalTextPrimary,
                                            fontSize = 14.sp
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = MinimalBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onSpeedSelected(speed)
                                    showDropdownMenu = false
                                },
                                modifier = Modifier.testTag("speed_dropdown_item_${label.replace(".", "_")}")
                            )
                        }
                    }
                }

                // 2. Status Pill
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
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(badgeText)
                        )
                        Text(
                            text = when {
                                isActuallyActive -> "${formatSpeedNumber(uiState.actualSpeed)}x"
                                uiState.status == SpeedStatus.NO_VIDEO -> "NO VIDEO"
                                uiState.status == SpeedStatus.CROSS_ORIGIN_IFRAME -> "IFRAME"
                                else -> "SYNC"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeText,
                            letterSpacing = 0.4.sp
                        )
                    }
                }

                // 3. Compact Quick Buttons (1x, 2x, 5x, 10x)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CompactSpeedChip(1.0, uiState.requestedSpeed, onSpeedSelected)
                    CompactSpeedChip(2.0, uiState.requestedSpeed, onSpeedSelected)
                    CompactSpeedChip(5.0, uiState.requestedSpeed, onSpeedSelected)

                    // Prominent Quick 10x button
                    val is10x = Math.abs(uiState.requestedSpeed - 10.0) < 0.01
                    Button(
                        onClick = { onSpeedSelected(10.0) },
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("quick_10x_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MinimalBlue,
                            contentColor = Color.White
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "10x",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Slider Toggle
                    IconButton(
                        onClick = { showCustomSlider = !showCustomSlider },
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("toggle_custom_slider_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Slider Settings",
                            tint = if (showCustomSlider) MinimalBlue else MinimalTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Diagnostics Modal
                    IconButton(
                        onClick = onOpenDiagnostics,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("open_diagnostics_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Developer Diagnostics",
                            tint = MinimalTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactSpeedChip(
    speed: Double,
    currentSpeed: Double,
    onSpeedSelected: (Double) -> Unit
) {
    val isSelected = Math.abs(currentSpeed - speed) < 0.01
    val label = formatSpeedNumber(speed) + "x"

    Surface(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSpeedSelected(speed) }
            .testTag("speed_chip_${label.replace(".", "_")}"),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MinimalBlue else Color(0xFFF1F5F9),
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
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
