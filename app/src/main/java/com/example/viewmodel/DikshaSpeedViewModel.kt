package com.example.viewmodel

import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.DiagnosticsInfo
import com.example.model.SpeedResult
import com.example.model.SpeedStatus
import com.example.model.SpeedUiState
import com.example.model.WebTab
import com.example.script.DikshaScript
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

class DikshaSpeedViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SpeedUiState())
    val uiState: StateFlow<SpeedUiState> = _uiState.asStateFlow()

    private var webViewRef: WebView? = null
    private var heartbeatJob: Job? = null

    init {
        startDiagnosticsHeartbeat()
    }

    fun attachWebView(webView: WebView) {
        webViewRef = webView
    }

    fun detachWebView() {
        webViewRef = null
    }

    fun updateUrlInput(text: String) {
        _uiState.update { it.copy(urlInputText = text) }
    }

    fun submitUrl(input: String? = null) {
        val target = (input ?: _uiState.value.urlInputText).trim()
        if (target.isEmpty()) return

        val formattedUrl = when {
            target.startsWith("http://") || target.startsWith("https://") -> target
            target.contains(".") && !target.contains(" ") -> "https://$target"
            else -> "https://www.google.com/search?q=${java.net.URLEncoder.encode(target, "UTF-8")}"
        }

        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) tab.copy(url = formattedUrl, title = extractHostOrTitle(formattedUrl)) else tab
            }
            state.copy(
                currentUrl = formattedUrl,
                urlInputText = formattedUrl,
                tabs = updatedTabs
            )
        }

        webViewRef?.loadUrl(formattedUrl)
    }

    fun addNewTab(url: String = "https://learning.diksha.gov.in/") {
        val newTab = WebTab(
            id = UUID.randomUUID().toString(),
            title = extractHostOrTitle(url),
            url = url
        )
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + newTab,
                activeTabId = newTab.id,
                currentUrl = url,
                urlInputText = url,
                status = SpeedStatus.PENDING
            )
        }
        webViewRef?.loadUrl(url)
    }

    fun selectTab(tabId: String) {
        val targetTab = _uiState.value.tabs.firstOrNull { it.id == tabId } ?: return
        if (targetTab.id == _uiState.value.activeTabId) return

        _uiState.update { state ->
            state.copy(
                activeTabId = targetTab.id,
                currentUrl = targetTab.url,
                urlInputText = targetTab.url,
                status = SpeedStatus.PENDING
            )
        }
        webViewRef?.loadUrl(targetTab.url)
    }

    fun closeTab(tabId: String) {
        _uiState.update { state ->
            val remainingTabs = state.tabs.filterNot { it.id == tabId }
            if (remainingTabs.isEmpty()) {
                val fallbackTab = WebTab(
                    id = UUID.randomUUID().toString(),
                    title = "DIKSHA",
                    url = "https://learning.diksha.gov.in/"
                )
                webViewRef?.loadUrl(fallbackTab.url)
                state.copy(
                    tabs = listOf(fallbackTab),
                    activeTabId = fallbackTab.id,
                    currentUrl = fallbackTab.url,
                    urlInputText = fallbackTab.url
                )
            } else {
                val nextActive = if (state.activeTabId == tabId) {
                    remainingTabs.last().id
                } else {
                    state.activeTabId
                }
                val activeTab = remainingTabs.first { it.id == nextActive }
                if (state.activeTabId == tabId) {
                    webViewRef?.loadUrl(activeTab.url)
                }
                state.copy(
                    tabs = remainingTabs,
                    activeTabId = nextActive,
                    currentUrl = activeTab.url,
                    urlInputText = activeTab.url
                )
            }
        }
    }

    fun onPageTitleChanged(title: String?) {
        if (title.isNullOrBlank()) return
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) tab.copy(title = title) else tab
            }
            state.copy(tabs = updatedTabs)
        }
    }

    fun onPageStarted(url: String) {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) tab.copy(url = url, title = extractHostOrTitle(url)) else tab
            }
            state.copy(
                currentUrl = url,
                urlInputText = url,
                tabs = updatedTabs,
                isPageLoading = true,
                pageProgress = 10,
                status = SpeedStatus.PENDING,
                statusMessage = "Loading page..."
            )
        }
    }

    fun onPageProgress(progress: Int) {
        _uiState.update {
            it.copy(
                pageProgress = progress,
                isPageLoading = progress < 100
            )
        }
    }

    fun onPageFinished(url: String, canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update { state ->
            val updatedTabs = state.tabs.map { tab ->
                if (tab.id == state.activeTabId) tab.copy(url = url) else tab
            }
            state.copy(
                currentUrl = url,
                urlInputText = url,
                tabs = updatedTabs,
                isPageLoading = false,
                pageProgress = 100,
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }

        // Inject script & re-apply current requested speed automatically
        injectAndReapplySpeed()
    }

    fun onHistoryChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update {
            it.copy(
                canGoBack = canGoBack,
                canGoForward = canGoForward
            )
        }
    }

    fun requestSpeed(speed: Double) {
        _uiState.update {
            it.copy(
                requestedSpeed = speed,
                statusMessage = "Setting speed to ${formatSpeed(speed)}..."
            )
        }

        val webView = webViewRef ?: return
        val jsCode = DikshaScript.buildSetSpeedCall(speed)
        addLog("Requesting speed: ${speed}x")

        webView.post {
            webView.evaluateJavascript(jsCode) { resultRaw ->
                if (resultRaw != null && resultRaw != "null") {
                    parseSpeedResultJson(unwrapJsString(resultRaw))
                }
            }
        }
    }

    fun refreshDiagnostics() {
        val webView = webViewRef ?: return
        val jsCode = DikshaScript.buildDiagnosticsCall()
        webView.post {
            webView.evaluateJavascript(jsCode) { resultRaw ->
                if (resultRaw != null && resultRaw != "null") {
                    parseDiagnosticsJson(unwrapJsString(resultRaw))
                }
            }
        }
    }

    fun handleSpeedBridgeResult(jsonString: String) {
        parseSpeedResultJson(jsonString)
    }

    fun handleDiagnosticsBridgeResult(jsonString: String) {
        parseDiagnosticsJson(jsonString)
    }

    fun setDiagnosticsOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isDiagnosticsOpen = isOpen) }
        if (isOpen) {
            refreshDiagnostics()
        }
    }

    fun setQuickHelpOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isQuickHelpOpen = isOpen) }
    }

    fun loadUrl(url: String) {
        submitUrl(url)
    }

    fun goBack() {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        }
    }

    fun goForward() {
        if (webViewRef?.canGoForward() == true) {
            webViewRef?.goForward()
        }
    }

    fun reload() {
        webViewRef?.reload()
    }

    fun goHome() {
        submitUrl("https://learning.diksha.gov.in/")
    }

    private fun injectAndReapplySpeed() {
        val webView = webViewRef ?: return
        val targetSpeed = _uiState.value.requestedSpeed
        webView.post {
            // First inject engine
            webView.evaluateJavascript(DikshaScript.INJECTION_SCRIPT, null)
            // Then apply target speed
            val applyScript = DikshaScript.buildSetSpeedCall(targetSpeed)
            webView.evaluateJavascript(applyScript) { resultRaw ->
                if (resultRaw != null && resultRaw != "null") {
                    parseSpeedResultJson(unwrapJsString(resultRaw))
                }
            }
        }
    }

    private fun startDiagnosticsHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (isActive) {
                delay(2000)
                if (webViewRef != null && !_uiState.value.isPageLoading) {
                    refreshDiagnostics()
                }
            }
        }
    }

    private fun parseSpeedResultJson(jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            val success = json.optBoolean("success", false)
            val requested = json.optDouble("requested", _uiState.value.requestedSpeed)
            val actual = json.optDouble("actual", 1.0)
            val reason = json.optString("reason", "")
            val videoCount = json.optInt("videoCount", 0)
            val isPlaying = json.optBoolean("isPlaying", false)
            val message = json.optString("message", "")

            val status = when {
                success && Math.abs(actual - requested) < 0.01 -> SpeedStatus.ACTIVE
                reason == "CROSS_ORIGIN_IFRAME" -> SpeedStatus.CROSS_ORIGIN_IFRAME
                reason == "NO_VIDEO" -> SpeedStatus.NO_VIDEO
                Math.abs(actual - requested) >= 0.01 -> SpeedStatus.RATE_MISMATCH
                else -> if (success) SpeedStatus.ACTIVE else SpeedStatus.ERROR
            }

            val statusMsg = when (status) {
                SpeedStatus.ACTIVE -> "Speed: ${formatSpeed(actual)} • Status: ACTIVE"
                SpeedStatus.NO_VIDEO -> "No video found on current page"
                SpeedStatus.CROSS_ORIGIN_IFRAME -> "Video is inside a cross-origin iframe."
                SpeedStatus.RATE_MISMATCH -> "Mismatch: requested ${formatSpeed(requested)}, actual ${formatSpeed(actual)}"
                SpeedStatus.ERROR -> if (message.isNotEmpty()) message else "Failed to apply speed"
                SpeedStatus.PENDING -> "Pending video detection"
            }

            addLog("Speed Response: success=$success, requested=$requested, actual=$actual, status=$status")

            _uiState.update { current ->
                current.copy(
                    requestedSpeed = requested,
                    actualSpeed = actual,
                    status = status,
                    statusMessage = statusMsg,
                    videoCount = videoCount,
                    isVideoPlaying = isPlaying,
                    diagnostics = current.diagnostics.copy(
                        videoFound = videoCount > 0,
                        videoCount = videoCount,
                        activeVideo = videoCount > 0,
                        isPlaying = isPlaying,
                        requestedSpeed = requested,
                        actualPlaybackRate = actual,
                        status = status.name
                    )
                )
            }
        } catch (e: Exception) {
            addLog("Error parsing speed JSON: ${e.message}")
        }
    }

    private fun parseDiagnosticsJson(jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            val url = json.optString("url", _uiState.value.currentUrl)
            val videoFound = json.optBoolean("videoFound", false)
            val videoCount = json.optInt("videoCount", 0)
            val activeVideo = json.optBoolean("activeVideo", false)
            val isPlaying = json.optBoolean("isPlaying", false)
            val requestedSpeed = json.optDouble("requestedSpeed", _uiState.value.requestedSpeed)
            val actualPlaybackRate = json.optDouble("actualPlaybackRate", 1.0)
            val isCrossOrigin = json.optBoolean("isCrossOriginIframe", false)
            val currentTime = json.optDouble("currentTime", 0.0)
            val duration = json.optDouble("duration", 0.0)

            val status = when {
                videoFound && Math.abs(actualPlaybackRate - requestedSpeed) < 0.01 -> SpeedStatus.ACTIVE
                isCrossOrigin -> SpeedStatus.CROSS_ORIGIN_IFRAME
                !videoFound -> SpeedStatus.NO_VIDEO
                Math.abs(actualPlaybackRate - requestedSpeed) >= 0.01 -> SpeedStatus.RATE_MISMATCH
                else -> SpeedStatus.PENDING
            }

            val statusMsg = when (status) {
                SpeedStatus.ACTIVE -> "Speed: ${formatSpeed(actualPlaybackRate)} • Status: ACTIVE"
                SpeedStatus.NO_VIDEO -> "No video found on current page"
                SpeedStatus.CROSS_ORIGIN_IFRAME -> "Video is inside a cross-origin iframe."
                SpeedStatus.RATE_MISMATCH -> "Mismatch: requested ${formatSpeed(requestedSpeed)}, actual ${formatSpeed(actualPlaybackRate)}"
                SpeedStatus.ERROR -> "Error detecting playback rate"
                SpeedStatus.PENDING -> "Scanning for video element..."
            }

            val diagInfo = DiagnosticsInfo(
                url = url,
                videoFound = videoFound,
                videoCount = videoCount,
                activeVideo = activeVideo,
                isPlaying = isPlaying,
                requestedSpeed = requestedSpeed,
                actualPlaybackRate = actualPlaybackRate,
                isCrossOriginIframe = isCrossOrigin,
                currentTime = currentTime,
                duration = duration,
                status = if (status == SpeedStatus.ACTIVE) "SUCCESS" else status.name
            )

            _uiState.update { current ->
                current.copy(
                    actualSpeed = if (videoFound) actualPlaybackRate else current.actualSpeed,
                    status = if (videoFound || current.status != SpeedStatus.ACTIVE) status else current.status,
                    statusMessage = statusMsg,
                    videoCount = videoCount,
                    isVideoPlaying = isPlaying,
                    diagnostics = diagInfo
                )
            }
        } catch (e: Exception) {
            // Ignore parse errors from background poll
        }
    }

    private fun extractHostOrTitle(url: String): String {
        return try {
            val uri = java.net.URI(url)
            val host = uri.host ?: url
            when {
                host.contains("diksha.gov.in") -> "DIKSHA"
                host.contains("youtube.com") || host.contains("youtu.be") -> "YouTube"
                host.contains("google.com") -> "Search"
                else -> host.removePrefix("www.").take(16)
            }
        } catch (e: Exception) {
            "New Tab"
        }
    }

    private fun addLog(msg: String) {
        _uiState.update { current ->
            val updated = (listOf(msg) + current.logMessages).take(30)
            current.copy(logMessages = updated)
        }
    }

    private fun unwrapJsString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("\"") && str.endsWith("\"") && str.length >= 2) {
            str = str.substring(1, str.length - 1)
            // Replace escaped quotes and slashes
            str = str.replace("\\\"", "\"").replace("\\\\", "\\")
        }
        return str
    }

    fun formatSpeed(speed: Double): String {
        return if (speed % 1.0 == 0.0) {
            "${speed.toInt()}x"
        } else {
            "${speed}x"
        }
    }

    override fun onCleared() {
        super.onCleared()
        heartbeatJob?.cancel()
        webViewRef = null
    }
}
