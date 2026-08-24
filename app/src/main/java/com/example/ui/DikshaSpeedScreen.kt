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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bridge.DikshaSpeedBridge
import com.example.model.SpeedStatus
import com.example.model.WebTab
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
    val focusManager = LocalFocusManager.current
    val tabsScrollState = rememberScrollState()

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
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Row 1: App Header with Tab Strip & "+" New Tab Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brand Logo
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MinimalBlue,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { viewModel.goHome() }
                                .testTag("app_brand_logo")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "D",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Scrollable Tabs Strip
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(tabsScrollState),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.tabs.forEach { tab ->
                                val isActive = tab.id == uiState.activeTabId
                                TabPill(
                                    tab = tab,
                                    isActive = isActive,
                                    onSelect = { viewModel.selectTab(tab.id) },
                                    onClose = { viewModel.closeTab(tab.id) }
                                )
                            }

                            // "+" Button to add new Tab
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.addNewTab("https://learning.diksha.gov.in/")
                                    }
                                    .testTag("add_tab_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "New Tab",
                                        tint = MinimalBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Help / Info
                        IconButton(
                            onClick = { viewModel.setQuickHelpOpen(true) },
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("help_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Help Guide",
                                tint = MinimalTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Row 2: Navigation controls & URL address bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Back
                        IconButton(
                            onClick = { viewModel.goBack() },
                            enabled = uiState.canGoBack,
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("nav_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (uiState.canGoBack) MinimalTextPrimary else Color(0xFFCBD5E1),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Forward
                        IconButton(
                            onClick = { viewModel.goForward() },
                            enabled = uiState.canGoForward,
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("nav_forward_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Forward",
                                tint = if (uiState.canGoForward) MinimalTextPrimary else Color(0xFFCBD5E1),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Reload
                        IconButton(
                            onClick = { viewModel.reload() },
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("reload_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = MinimalTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // URL Input Omnibox Field
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.urlInputText.startsWith("https://")) Icons.Default.Lock else Icons.Default.Public,
                                    contentDescription = "Security",
                                    tint = if (uiState.urlInputText.startsWith("https://")) Color(0xFF059669) else MinimalTextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )

                                BasicTextField(
                                    value = uiState.urlInputText,
                                    onValueChange = { viewModel.updateUrlInput(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("url_input_field"),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = MinimalTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(MinimalBlue),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Uri,
                                        imeAction = ImeAction.Go
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onGo = {
                                            viewModel.submitUrl()
                                            focusManager.clearFocus()
                                        }
                                    ),
                                    decorationBox = { innerTextField ->
                                        if (uiState.urlInputText.isEmpty()) {
                                            Text(
                                                text = "Enter website URL (e.g. diksha.gov.in)...",
                                                color = MinimalTextMuted,
                                                fontSize = 12.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )

                                if (uiState.urlInputText.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MinimalTextMuted,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { viewModel.updateUrlInput("") }
                                    )
                                }

                                // Go Button
                                Surface(
                                    shape = CircleShape,
                                    color = MinimalBlue,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clickable {
                                            viewModel.submitUrl()
                                            focusManager.clearFocus()
                                        }
                                        .testTag("url_go_button")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "Go",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Progress bar
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

                        // Configure WebSettings for full modern HTML5 video & external sites compatibility
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
                            userAgentString = userAgentString.replace("; wv", "") // Desktop/mobile clean UA
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
                                return false // Handle navigation seamlessly in WebView
                            }
                        }

                        // WebChromeClient for page loading progress & title
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                viewModel.onPageProgress(newProgress)
                            }

                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                super.onReceivedTitle(view, title)
                                viewModel.onPageTitleChanged(title)
                            }
                        }

                        // Attach to viewModel
                        viewModel.attachWebView(this)

                        // Load initial active tab URL
                        loadUrl(uiState.currentUrl)
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

@Composable
private fun TabPill(
    tab: WebTab,
    isActive: Boolean,
    onSelect: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) Color(0xFFD1E4FF) else Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, if (isActive) MinimalBlue.copy(alpha = 0.5f) else Color(0xFFE2E8F0)),
        modifier = Modifier
            .height(28.dp)
            .widthIn(min = 60.dp, max = 120.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .testTag("tab_item_${tab.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = tab.title,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = if (isActive) MinimalBlue else MinimalTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close tab",
                tint = if (isActive) MinimalBlue else MinimalTextMuted,
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onClose() }
            )
        }
    }
}
