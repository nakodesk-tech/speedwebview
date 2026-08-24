package com.example.model

data class SpeedResult(
    val success: Boolean = false,
    val requested: Double = 1.0,
    val actual: Double = 1.0,
    val reason: String = "INITIAL",
    val videoCount: Int = 0,
    val isPlaying: Boolean = false,
    val currentTime: Double = 0.0,
    val duration: Double = 0.0,
    val message: String = ""
)

data class DiagnosticsInfo(
    val url: String = "https://learning.diksha.gov.in/",
    val videoFound: Boolean = false,
    val videoCount: Int = 0,
    val activeVideo: Boolean = false,
    val isPlaying: Boolean = false,
    val requestedSpeed: Double = 1.0,
    val actualPlaybackRate: Double = 1.0,
    val isCrossOriginIframe: Boolean = false,
    val currentTime: Double = 0.0,
    val duration: Double = 0.0,
    val status: String = "PENDING"
)

enum class SpeedStatus {
    ACTIVE,
    NO_VIDEO,
    CROSS_ORIGIN_IFRAME,
    RATE_MISMATCH,
    PENDING,
    ERROR
}

data class SpeedUiState(
    val currentUrl: String = "https://learning.diksha.gov.in/",
    val requestedSpeed: Double = 1.0,
    val actualSpeed: Double = 1.0,
    val status: SpeedStatus = SpeedStatus.PENDING,
    val statusMessage: String = "Ready to detect video",
    val videoCount: Int = 0,
    val isVideoPlaying: Boolean = false,
    val isPageLoading: Boolean = false,
    val pageProgress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val diagnostics: DiagnosticsInfo = DiagnosticsInfo(),
    val isDiagnosticsOpen: Boolean = false,
    val isQuickHelpOpen: Boolean = false,
    val logMessages: List<String> = emptyList()
) {
    val isActive: Boolean
        get() = status == SpeedStatus.ACTIVE && Math.abs(actualSpeed - requestedSpeed) < 0.01
}

val PRESET_SPEEDS = listOf(
    1.0,
    1.25,
    1.5,
    1.75,
    2.0,
    2.5,
    3.0,
    4.0,
    5.0,
    7.5,
    10.0
)
