package com.example.engine2.model

enum class SunbirdSpeedStatus {
    PENDING,
    LOADING,
    READY,
    SUCCESS,
    RATE_MISMATCH,
    NO_VIDEO,
    ERROR
}

data class SunbirdPlayerConfig(
    val identifier: String = "do_313014389012345678",
    val title: String = "Sunbird Video Player Test (Big Buck Bunny)",
    val artifactUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
    val mimeType: String = "video/mp4",
    val streamingUrl: String = "",
    val pkgVersion: Int = 1
)

data class SunbirdDiagnostics(
    val playerVersion: String = "@project-sunbird/sunbird-video-player-web-component@latest",
    val videoJsVersion: String = "video.js@7.21.5",
    val customElementRegistered: Boolean = false,
    val playerElementFound: Boolean = false,
    val underlyingVideoFound: Boolean = false,
    val videoElementId: String = "",
    val videoJsInstanceFound: Boolean = false,
    val videoJsPlayerId: String = "",
    val isPlaying: Boolean = false,
    val currentTime: Double = 0.0,
    val duration: Double = 0.0,
    val requestedSpeed: Double = 1.0,
    val actualPlaybackRate: Double = 1.0,
    val accessMethodUsed: String = "Direct HTML5 Video Element & Video.js tech",
    val status: SunbirdSpeedStatus = SunbirdSpeedStatus.PENDING,
    val lastMessage: String = "Initializing Sunbird Player..."
)

data class SunbirdEngineUiState(
    val selectedVideoSampleIndex: Int = 0,
    val requestedSpeed: Double = 1.0,
    val actualSpeed: Double = 1.0,
    val status: SunbirdSpeedStatus = SunbirdSpeedStatus.PENDING,
    val statusMessage: String = "Initializing Sunbird Player environment...",
    val diagnostics: SunbirdDiagnostics = SunbirdDiagnostics(),
    val eventLogs: List<String> = emptyList(),
    val telemetryLogs: List<String> = emptyList(),
    val customArtifactUrl: String = "",
    val isCustomConfigOpen: Boolean = false
)

data class VideoSampleOption(
    val name: String,
    val url: String,
    val mimeType: String,
    val identifier: String
)

val DEFAULT_VIDEO_SAMPLES = listOf(
    VideoSampleOption(
        name = "Big Buck Bunny (MP4)",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        mimeType = "video/mp4",
        identifier = "do_sample_bbb_01"
    ),
    VideoSampleOption(
        name = "Elephants Dream (MP4)",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        mimeType = "video/mp4",
        identifier = "do_sample_ed_02"
    ),
    VideoSampleOption(
        name = "For Bigger Blazes (MP4)",
        url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        mimeType = "video/mp4",
        identifier = "do_sample_fbb_03"
    )
)
