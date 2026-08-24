package com.example.engine2.ui

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine2.bridge.SunbirdBridge
import com.example.engine2.model.DEFAULT_VIDEO_SAMPLES
import com.example.engine2.model.SunbirdEngineUiState
import com.example.engine2.model.SunbirdSpeedStatus
import com.example.engine2.viewmodel.SunbirdEngineViewModel
import com.example.ui.theme.MinimalBackground
import com.example.ui.theme.MinimalBlue
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SunbirdEngineScreen(
    viewModel: SunbirdEngineViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTechDetails by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalBackground),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, MinimalBorder),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    // Header Row
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
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0284C7),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Science,
                                        contentDescription = "Engine 2 Lab",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Sunbird Video Player Web Component",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalTextPrimary
                                )
                                Text(
                                    text = "Engine 2 • Video.js & Web Component Sandbox",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF0284C7)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.reloadCurrentPlayerConfig() },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload Player",
                                    tint = MinimalTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { showTechDetails = !showTechDetails },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = "Toggle Tech Details",
                                    tint = if (showTechDetails) Color(0xFF0284C7) else MinimalTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Content Sample Selector Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DEFAULT_VIDEO_SAMPLES.forEachIndexed { index, sample ->
                            val isSelected = index == uiState.selectedVideoSampleIndex
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) Color(0xFFE0F2FE) else Color(0xFFF1F5F9),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF0284C7) else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { viewModel.selectSample(index) }
                                    .testTag("sample_chip_$index")
                            ) {
                                Text(
                                    text = sample.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF0369A1) else MinimalTextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Speed Test Controls & Success Status
            SunbirdSpeedBottomPanel(
                uiState = uiState,
                onSpeedSelected = { speed -> viewModel.requestSpeed(speed) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F172A))
        ) {
            // 1. Verification Result Bar (Requested, Actual, Status)
            SunbirdVerificationHeader(uiState = uiState)

            // 2. Main Player WebView Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (showTechDetails) 1.2f else 2f)
                    .background(Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("sunbird_player_webview"),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                allowFileAccess = true
                                allowContentAccess = true
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            }

                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            val bridge = SunbirdBridge(
                                onSpeedVerificationCallback = { json ->
                                    post { viewModel.handleSpeedVerification(json) }
                                },
                                onDiagnosticsCallback = { json ->
                                    post { viewModel.handleDiagnostics(json) }
                                },
                                onPlayerEventCallback = { json ->
                                    post { viewModel.handlePlayerEvent(json) }
                                },
                                onTelemetryEventCallback = { json ->
                                    post { viewModel.handleTelemetryEvent(json) }
                                },
                                onLogCallback = { msg ->
                                    post { viewModel.handleLog(msg) }
                                }
                            )
                            addJavascriptInterface(bridge, "SunbirdBridge")

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    viewModel.refreshDiagnostics()
                                }
                            }

                            webChromeClient = WebChromeClient()

                            viewModel.attachWebView(this)
                        }
                    },
                    update = { webView ->
                        viewModel.attachWebView(webView)
                    }
                )
            }

            // 3. Technical Inspector & Logs (Scrollable if enabled)
            if (showTechDetails) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFF8FAFC))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, MinimalBorder),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "RESEARCH & INSPECTION DATA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0284C7),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TechRow("Package", uiState.diagnostics.playerVersion)
                                TechRow("Video.js Engine", uiState.diagnostics.videoJsVersion)
                                TechRow("Custom Element", if (uiState.diagnostics.customElementRegistered) "<sunbird-video-player> [REGISTERED]" else "Registering...")
                                TechRow("Underlying Video ID", uiState.diagnostics.videoElementId)
                                TechRow("Video.js Player ID", uiState.diagnostics.videoJsPlayerId)
                                TechRow("Speed Access Method", uiState.diagnostics.accessMethodUsed)
                            }
                        }
                    }

                    if (uiState.eventLogs.isNotEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, MinimalBorder),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "PLAYER & TELEMETRY EVENTS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MinimalTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    uiState.eventLogs.take(4).forEach { log ->
                                        Text(
                                            text = "• $log",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MinimalTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SunbirdVerificationHeader(uiState: SunbirdEngineUiState) {
    val isSuccess = uiState.status == SunbirdSpeedStatus.SUCCESS &&
            Math.abs(uiState.actualSpeed - uiState.requestedSpeed) < 0.01

    val reqFormatted = if (uiState.requestedSpeed % 1.0 == 0.0) "${uiState.requestedSpeed.toInt()}x" else "${uiState.requestedSpeed}x"
    val actFormatted = if (uiState.actualSpeed % 1.0 == 0.0) "${uiState.actualSpeed.toInt()}x" else "${uiState.actualSpeed}x"

    Surface(
        color = if (isSuccess) Color(0xFF064E3B) else Color(0xFF1E293B),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Requested
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Requested: ",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = reqFormatted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Actual
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Actual: ",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = actFormatted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccess) Color(0xFF4ADE80) else Color(0xFFFBBF24),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = if (isSuccess) Color(0xFF059669) else Color(0xFFD97706)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isSuccess) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Text(
                        text = if (isSuccess) "Status: SUCCESS" else "Status: ${uiState.status.name}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.4.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SunbirdSpeedBottomPanel(
    uiState: SunbirdEngineUiState,
    onSpeedSelected: (Double) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp)
            .testTag("sunbird_speed_panel"),
        color = Color.White,
        border = BorderStroke(1.dp, MinimalBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SUNBIRD SPEED CONTROLS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = MinimalTextMuted,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Active: ${if (uiState.actualSpeed % 1.0 == 0.0) "${uiState.actualSpeed.toInt()}x" else "${uiState.actualSpeed}x"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.status == SunbirdSpeedStatus.SUCCESS) StatusActiveGreen else MinimalTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Required Test Controls: 1x, 2x, 5x, 10x
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1.0, 2.0, 5.0, 10.0).forEach { speed ->
                    val isSelected = Math.abs(uiState.requestedSpeed - speed) < 0.01
                    val label = if (speed == 10.0) "10x" else "${speed.toInt()}x"

                    Button(
                        onClick = { onSpeedSelected(speed) },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("sunbird_btn_$label"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) {
                                if (speed == 10.0) Color(0xFF0284C7) else MinimalBlue
                            } else {
                                Color(0xFFF1F5F9)
                            },
                            contentColor = if (isSelected) Color.White else MinimalTextPrimary
                        ),
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        if (speed == 10.0) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = if (isSelected) Color.White else MinimalBlue
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TechRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MinimalTextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MinimalTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
