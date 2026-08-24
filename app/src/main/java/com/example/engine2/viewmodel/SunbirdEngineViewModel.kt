package com.example.engine2.viewmodel

import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine2.model.DEFAULT_VIDEO_SAMPLES
import com.example.engine2.model.SunbirdDiagnostics
import com.example.engine2.model.SunbirdEngineUiState
import com.example.engine2.model.SunbirdPlayerConfig
import com.example.engine2.model.SunbirdSpeedStatus
import com.example.engine2.script.SunbirdHtmlProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

class SunbirdEngineViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SunbirdEngineUiState())
    val uiState: StateFlow<SunbirdEngineUiState> = _uiState.asStateFlow()

    private var webViewRef: WebView? = null
    private var heartbeatJob: Job? = null
    private var isLoaded: Boolean = false

    init {
        startDiagnosticsHeartbeat()
    }

    fun attachWebView(webView: WebView) {
        val isFirstAttach = (webViewRef !== webView)
        webViewRef = webView
        if (isFirstAttach && !isLoaded) {
            isLoaded = true
            reloadCurrentPlayerConfig()
        }
    }

    fun detachWebView() {
        webViewRef = null
    }

    fun selectSample(index: Int) {
        if (index < 0 || index >= DEFAULT_VIDEO_SAMPLES.size) return
        val sample = DEFAULT_VIDEO_SAMPLES[index]
        _uiState.update {
            it.copy(
                selectedVideoSampleIndex = index,
                status = SunbirdSpeedStatus.LOADING,
                statusMessage = "Loading Sunbird configuration for ${sample.name}..."
            )
        }
        reloadCurrentPlayerConfig()
    }

    fun updateCustomArtifactUrl(url: String) {
        _uiState.update { it.copy(customArtifactUrl = url) }
    }

    fun applyCustomArtifactUrl() {
        val url = _uiState.value.customArtifactUrl.trim()
        if (url.isEmpty()) return
        _uiState.update {
            it.copy(
                isCustomConfigOpen = false,
                status = SunbirdSpeedStatus.LOADING,
                statusMessage = "Loading Sunbird Player with custom URL..."
            )
        }
        reloadCurrentPlayerConfig(customUrl = url)
    }

    fun setCustomConfigOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isCustomConfigOpen = isOpen) }
    }

    fun requestSpeed(speed: Double) {
        _uiState.update {
            it.copy(
                requestedSpeed = speed,
                statusMessage = "Applying speed ${formatSpeed(speed)} to Sunbird Video.js player..."
            )
        }

        val webView = webViewRef ?: return
        val jsCode = "window.setSunbirdPlaybackSpeed && window.setSunbirdPlaybackSpeed($speed);"
        webView.post {
            webView.evaluateJavascript(jsCode) { rawResult ->
                if (rawResult != null && rawResult != "null") {
                    parseSpeedVerificationJson(unwrapJsString(rawResult))
                }
            }
        }
    }

    fun refreshDiagnostics() {
        val webView = webViewRef ?: return
        val jsCode = "window.getSunbirdDiagnostics && window.getSunbirdDiagnostics();"
        webView.post {
            webView.evaluateJavascript(jsCode) { rawResult ->
                if (rawResult != null && rawResult != "null") {
                    parseDiagnosticsJson(unwrapJsString(rawResult))
                }
            }
        }
    }

    fun reloadCurrentPlayerConfig(customUrl: String? = null) {
        val sample = DEFAULT_VIDEO_SAMPLES.getOrNull(_uiState.value.selectedVideoSampleIndex) ?: DEFAULT_VIDEO_SAMPLES[0]
        val activeUrl = customUrl ?: sample.url
        val activeName = if (customUrl != null) "Custom Sunbird Video" else sample.name

        val config = SunbirdPlayerConfig(
            identifier = sample.identifier,
            title = activeName,
            artifactUrl = activeUrl,
            mimeType = sample.mimeType
        )

        val html = SunbirdHtmlProvider.buildHtmlDocument(
            config = config,
            initialSpeed = _uiState.value.requestedSpeed
        )

        webViewRef?.post {
            webViewRef?.loadDataWithBaseURL(
                "file:///android_asset/sunbird/",
                html,
                "text/html",
                "UTF-8",
                null
            )
        }
    }

    fun handleSpeedVerification(jsonStr: String) {
        parseSpeedVerificationJson(jsonStr)
    }

    fun handleDiagnostics(jsonStr: String) {
        parseDiagnosticsJson(jsonStr)
    }

    fun handlePlayerEvent(jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            val eventType = json.optString("type", json.optString("event", "EVENT"))
            val msg = "Player Event: $eventType"
            _uiState.update { current ->
                val updatedLogs = (listOf(msg) + current.eventLogs).take(30)
                current.copy(eventLogs = updatedLogs)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun handleTelemetryEvent(jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            val eid = json.optString("eid", "INTERACT")
            val msg = "Telemetry: $eid [${json.optString("ets", "")}]"
            _uiState.update { current ->
                val updatedLogs = (listOf(msg) + current.telemetryLogs).take(30)
                current.copy(telemetryLogs = updatedLogs)
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun handleLog(msg: String) {
        _uiState.update { current ->
            val updated = (listOf(msg) + current.eventLogs).take(30)
            current.copy(eventLogs = updated)
        }
    }

    private fun parseSpeedVerificationJson(jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            val requested = json.optDouble("requestedSpeed", json.optDouble("requested", _uiState.value.requestedSpeed))
            val actual = json.optDouble("actualSpeed", json.optDouble("actual", 1.0))
            val success = json.optBoolean("success", false)
            val method = json.optString("method", "Direct Video playbackRate & Video.js tech")
            val videoId = json.optString("videoId", "")
            val vjsId = json.optString("vjsPlayerId", "")

            val isMatch = Math.abs(actual - requested) < 0.01
            val status = if (success && isMatch) {
                SunbirdSpeedStatus.SUCCESS
            } else if (!isMatch) {
                SunbirdSpeedStatus.RATE_MISMATCH
            } else {
                SunbirdSpeedStatus.ERROR
            }

            val statusMsg = if (status == SunbirdSpeedStatus.SUCCESS) {
                "Requested: ${formatSpeed(requested)} | Actual: ${formatSpeed(actual)} | Status: SUCCESS"
            } else {
                "Rate Mismatch: Requested ${formatSpeed(requested)}, Actual ${formatSpeed(actual)}"
            }

            _uiState.update { current ->
                current.copy(
                    requestedSpeed = requested,
                    actualSpeed = actual,
                    status = status,
                    statusMessage = statusMsg,
                    diagnostics = current.diagnostics.copy(
                        requestedSpeed = requested,
                        actualPlaybackRate = actual,
                        videoElementId = if (videoId.isNotEmpty()) videoId else current.diagnostics.videoElementId,
                        videoJsPlayerId = if (vjsId.isNotEmpty()) vjsId else current.diagnostics.videoJsPlayerId,
                        accessMethodUsed = method,
                        status = status,
                        lastMessage = statusMsg
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun parseDiagnosticsJson(jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            val customReg = json.optBoolean("customElementRegistered", json.optBoolean("customElementDefined", false))
            val playerElemFound = json.optBoolean("playerElementFound", json.optBoolean("playerElementPresent", false))
            val videoFound = json.optBoolean("underlyingVideoFound", json.optBoolean("videoElementPresent", false))
            val videoId = json.optString("videoElementId", "")
            val vjsFound = json.optBoolean("videoJsInstanceFound", false)
            val vjsId = json.optString("videoJsPlayerId", "")
            val isPlaying = json.optBoolean("isPlaying", false)
            val currentTime = json.optDouble("currentTime", 0.0)
            val duration = json.optDouble("duration", 0.0)
            val requested = json.optDouble("requestedSpeed", _uiState.value.requestedSpeed)
            val actual = json.optDouble("actualPlaybackRate", json.optDouble("currentPlaybackRate", 1.0))
            val method = json.optString("accessMethodUsed", "Direct HTML5 Video Element & Video.js tech")

            val status = if (videoFound && Math.abs(actual - requested) < 0.01) {
                SunbirdSpeedStatus.SUCCESS
            } else if (!videoFound) {
                SunbirdSpeedStatus.NO_VIDEO
            } else if (Math.abs(actual - requested) >= 0.01) {
                SunbirdSpeedStatus.RATE_MISMATCH
            } else {
                SunbirdSpeedStatus.READY
            }

            val diag = SunbirdDiagnostics(
                customElementRegistered = customReg,
                playerElementFound = playerElemFound,
                underlyingVideoFound = videoFound,
                videoElementId = videoId,
                videoJsInstanceFound = vjsFound,
                videoJsPlayerId = vjsId,
                isPlaying = isPlaying,
                currentTime = currentTime,
                duration = duration,
                requestedSpeed = requested,
                actualPlaybackRate = actual,
                accessMethodUsed = method,
                status = status,
                lastMessage = if (status == SunbirdSpeedStatus.SUCCESS) "Verified at speed ${formatSpeed(actual)}" else "Diagnostics active"
            )

            _uiState.update { current ->
                current.copy(
                    actualSpeed = if (videoFound) actual else current.actualSpeed,
                    status = if (videoFound) status else current.status,
                    diagnostics = diag
                )
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun startDiagnosticsHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (isActive) {
                delay(2000)
                if (webViewRef != null) {
                    refreshDiagnostics()
                }
            }
        }
    }

    fun formatSpeed(speed: Double): String {
        return if (speed % 1.0 == 0.0) {
            "${speed.toInt()}x"
        } else {
            "${speed}x"
        }
    }

    private fun unwrapJsString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("\"") && str.endsWith("\"") && str.length >= 2) {
            str = str.substring(1, str.length - 1)
            str = str.replace("\\\"", "\"").replace("\\\\", "\\")
        }
        return str
    }

    override fun onCleared() {
        super.onCleared()
        heartbeatJob?.cancel()
        webViewRef = null
    }
}
