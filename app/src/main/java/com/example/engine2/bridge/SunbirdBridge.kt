package com.example.engine2.bridge

import android.webkit.JavascriptInterface

class SunbirdBridge(
    private val onSpeedVerificationCallback: (String) -> Unit,
    private val onDiagnosticsCallback: (String) -> Unit,
    private val onPlayerEventCallback: (String) -> Unit,
    private val onTelemetryEventCallback: (String) -> Unit,
    private val onLogCallback: (String) -> Unit
) {

    @JavascriptInterface
    fun onSpeedVerification(jsonString: String) {
        onSpeedVerificationCallback(jsonString)
    }

    @JavascriptInterface
    fun onSpeedResult(jsonString: String) {
        onSpeedVerificationCallback(jsonString)
    }

    @JavascriptInterface
    fun onDiagnostics(jsonString: String) {
        onDiagnosticsCallback(jsonString)
    }

    @JavascriptInterface
    fun onPlayerEvent(jsonString: String) {
        onPlayerEventCallback(jsonString)
    }

    @JavascriptInterface
    fun onTelemetryEvent(jsonString: String) {
        onTelemetryEventCallback(jsonString)
    }

    @JavascriptInterface
    fun log(message: String) {
        onLogCallback(message)
    }
}
