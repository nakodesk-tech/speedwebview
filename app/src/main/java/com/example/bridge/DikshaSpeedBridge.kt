package com.example.bridge

import android.webkit.JavascriptInterface

class DikshaSpeedBridge(
    private val onSpeedResultJson: (String) -> Unit,
    private val onDiagnosticsJson: (String) -> Unit,
    private val onLogMessage: (String) -> Unit = {}
) {
    @JavascriptInterface
    fun onSpeedChanged(json: String) {
        onSpeedResultJson(json)
    }

    @JavascriptInterface
    fun onDiagnosticsUpdated(json: String) {
        onDiagnosticsJson(json)
    }

    @JavascriptInterface
    fun log(msg: String) {
        onLogMessage(msg)
    }
}
