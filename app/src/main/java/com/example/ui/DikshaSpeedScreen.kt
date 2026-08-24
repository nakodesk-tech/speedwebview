package com.example.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bridge.DikshaSpeedBridge
import com.example.model.SpeedStatus
import com.example.ui.components.DiagnosticsDialog
import com.example.ui.components.QuickHelpDialog
import com.example.ui.components.SpeedControlPanel
import com.example.ui.theme.MinimalBackground
import com.example.ui.theme.MinimalBlue
import com.example.ui.theme.MinimalBorder
import com.example.ui.theme.MinimalTextMuted
import com.example.ui.theme.MinimalTextPrimary
import com.example.ui.theme.MinimalTextSecondary
import com.example.ui.theme.StatusActiveGreen
import com.example.ui.theme.StatusActiveGreenBg
import com.example.ui.theme.StatusActiveGreenBorder
import com.example.viewmodel.DikshaSpeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DikshaSpeedScreen(
    viewModel: DikshaSpeedViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLive = uiState.status == SpeedStatus.ACTIVE && Math.abs(uiState.actualSpeed - uiState.requestedSpeed) < 0.01

    // Handle back button for WebView navigation
    BackHandler(enabled = uiState.canGoBack) {
        viewModel.goBack()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MinimalBackground),
        topBar = {
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, MinimalBorder),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Logo & Package title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MinimalBlue,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "D",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Speed Controller",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MinimalTextPrimary,
                                    lineHeight = 16.sp
                                )
                                Text(
                                    text = "com.nakodesk.dikshaspeed",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MinimalTextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Right: Live Status & Nav Actions
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Live / Ready indicator
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = if (isLive) StatusActiveGreenBg else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isLive) StatusActiveGreenBorder else Color(0xFFE2E8F0)),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isLive) StatusActiveGreen else Color(0xFF94A3B8))
                                    )
                                    Text(
                                        text = if (isLive) "LIVE" else "READY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isLive) StatusActiveGreen else MinimalTextSecondary
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.goHome() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("home_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "DIKSHA Home",
                                    tint = MinimalTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.goBack() },
                                enabled = uiState.canGoBack,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("nav_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = if (uiState.canGoBack) MinimalTextPrimary else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.reload() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("reload_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload",
                                    tint = MinimalTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { viewModel.setQuickHelpOpen(true) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("help_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HelpOutline,
                                    contentDescription = "Help Guide",
                                    tint = MinimalTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Clean URL capsule sub-bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8F9FB))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure",
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (uiState.currentUrl.contains("diksha.gov.in")) uiState.currentUrl else "https://learning.diksha.gov.in/",
                            fontSize = 11.sp,
                            color = MinimalTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (uiState.isPageLoading) {
                        LinearProgressIndicator(
                            progress = { uiState.pageProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp),
                            color = MinimalBlue,
                            trackColor = Color.Transparent
                        )
                    }
                }
            }
        },
        bottomBar = {
            SpeedControlPanel(
                uiState = uiState,
                onSpeedSelected = { speed -> viewModel.requestSpeed(speed) },
                onRefreshDiagnostics = { viewModel.refreshDiagnostics() },
                onOpenDiagnostics = { viewModel.setDiagnosticsOpen(true) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFE2E8F0))
        ) {
            // Main WebView
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("diksha_webview"),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // Configure WebSettings for full modern HTML5 video & DIKSHA compatibility
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                            allowFileAccess = true
                            allowContentAccess = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            cacheMode = WebSettings.LOAD_DEFAULT
                        }

                        // Enable Cookies including 3rd party cookies
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        // Attach JS Bridge
                        val bridge = DikshaSpeedBridge(
                            onSpeedResultJson = { json ->
                                post { viewModel.handleSpeedBridgeResult(json) }
                            },
                            onDiagnosticsJson = { json ->
                                post { viewModel.handleDiagnosticsBridgeResult(json) }
                            }
                        )
                        addJavascriptInterface(bridge, "DikshaSpeedBridge")

                        // WebViewClient for navigation and script injection
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                url?.let { viewModel.onPageStarted(it) }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                url?.let {
                                    viewModel.onPageFinished(
                                        url = it,
                                        canGoBack = canGoBack(),
                                        canGoForward = canGoForward()
                                    )
                                }
                            }

                            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                                super.doUpdateVisitedHistory(view, url, isReload)
                                viewModel.onHistoryChanged(
                                    canGoBack = canGoBack(),
                                    canGoForward = canGoForward()
                                )
                            }

                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                return false // Let WebView handle all redirects and navigation inside DIKSHA
                            }
                        }

                        // WebChromeClient for page loading progress
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                viewModel.onPageProgress(newProgress)
                            }
                        }

                        // Attach to viewModel
                        viewModel.attachWebView(this)

                        // Load initial DIKSHA URL
                        loadUrl("https://learning.diksha.gov.in/")
                    }
                },
                update = { webView ->
                    viewModel.attachWebView(webView)
                }
            )
        }
    }

    // Diagnostics Dialog
    if (uiState.isDiagnosticsOpen) {
        DiagnosticsDialog(
            uiState = uiState,
            onDismiss = { viewModel.setDiagnosticsOpen(false) },
            onRefresh = { viewModel.refreshDiagnostics() },
            onTestSpeed = { speed -> viewModel.requestSpeed(speed) }
        )
    }

    // Quick Help Dialog
    if (uiState.isQuickHelpOpen) {
        QuickHelpDialog(
            onDismiss = { viewModel.setQuickHelpOpen(false) }
        )
    }
}
