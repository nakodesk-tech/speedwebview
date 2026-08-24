package com.example.engine2.script

import com.example.engine2.model.SunbirdPlayerConfig
import org.json.JSONArray
import org.json.JSONObject

object SunbirdHtmlProvider {

    fun generatePlayerConfigJson(config: SunbirdPlayerConfig): String {
        val root = JSONObject()

        // 1. context
        val context = JSONObject().apply {
            put("mode", "play")
            put("partner", JSONArray())
            put("pdata", JSONObject().apply {
                put("id", "org.sunbird.app")
                put("ver", "5.1.0")
                put("pid", "sunbird.app.contentplayer")
            })
            put("contentId", config.identifier)
            put("sid", "session_${System.currentTimeMillis()}")
            put("uid", "anonymous_user")
            put("timeDiff", 0)
            put("contextRollup", JSONObject().apply {
                put("l1", config.identifier)
            })
            put("host", "")
            put("endpoint", "/data/v3/telemetry")
            put("tags", JSONArray())
            put("cdata", JSONArray())
        }
        root.put("context", context)

        // 2. config
        val playerConf = JSONObject().apply {
            put("traceId", "trace_${System.currentTimeMillis()}")
            put("sideMenu", JSONObject().apply {
                put("showShare", true)
                put("showDownload", true)
                put("showReplay", true)
                put("showExit", true)
            })
        }
        root.put("config", playerConf)

        // 3. metadata
        val metadata = JSONObject().apply {
            put("identifier", config.identifier)
            put("name", config.title)
            put("artifactUrl", config.artifactUrl)
            put("streamingUrl", config.streamingUrl)
            put("mimeType", config.mimeType)
            put("pkgVersion", config.pkgVersion)
            put("mediaType", "content")
            put("contentType", "Resource")
            put("primaryCategory", "Learning Resource")
        }
        root.put("metadata", metadata)

        return root.toString()
    }

    fun buildHtmlDocument(config: SunbirdPlayerConfig, initialSpeed: Double = 1.0): String {
        val configJson = generatePlayerConfigJson(config)
        // Escape JSON for HTML attribute
        val escapedConfig = configJson
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>${config.title}</title>
    
    <!-- Dependencies for Sunbird Video Player Web Component -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/video.js@7.21.5/dist/video-js.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@project-sunbird/sunbird-video-player-web-component@latest/styles.css">
    
    <script src="https://cdn.jsdelivr.net/npm/reflect-metadata@0.1.13/Reflect.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/video.js@7.21.5/dist/video.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@project-sunbird/sunbird-video-player-web-component@latest/sunbird-video-player.js"></script>

    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }
        body, html {
            width: 100%;
            height: 100%;
            overflow: hidden;
            background-color: #000000;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        }
        #player-wrapper {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #000;
        }
        sunbird-video-player {
            display: block;
            width: 100%;
            height: 100%;
        }
        .sunbird-video-container {
            width: 100%;
            height: 100%;
        }
        /* Fallback HTML5 Player in case custom element web component is still mounting */
        #fallback-video-container {
            display: none;
            width: 100%;
            height: 100%;
        }
        #fallback-video-container video {
            width: 100%;
            height: 100%;
            object-fit: contain;
        }
        #watermark-overlay {
            position: absolute;
            top: 10px;
            left: 12px;
            z-index: 9999;
            background: rgba(0, 0, 0, 0.7);
            color: #38bdf8;
            font-size: 11px;
            font-weight: 700;
            padding: 4px 8px;
            border-radius: 4px;
            pointer-events: none;
            border: 1px solid rgba(56, 189, 248, 0.3);
            letter-spacing: 0.5px;
        }
    </style>
</head>
<body>
    <div id="watermark-overlay">SUNBIRD WEB COMPONENT ENGINE 2</div>

    <div id="player-wrapper">
        <!-- Official Sunbird Video Player Web Component Element -->
        <sunbird-video-player id="sunbird-player" player-config="$escapedConfig"></sunbird-video-player>

        <!-- Fallback Video Element Container for zero-downtime reliability -->
        <div id="fallback-video-container">
            <video id="direct-video" controls playsinline preload="auto" src="${config.artifactUrl}"></video>
        </div>
    </div>

    <script>
        (function() {
            var TARGET_SPEED = $initialSpeed;
            var playerElement = document.getElementById('sunbird-player');
            var isWebComponentRegistered = typeof customElements !== 'undefined' && customElements.get('sunbird-video-player');

            function logBridge(tag, data) {
                if (window.SunbirdBridge && window.SunbirdBridge.log) {
                    window.SunbirdBridge.log('[' + tag + '] ' + JSON.stringify(data));
                }
            }

            // Register Sunbird custom element fallback if CDN is blocked/delayed
            if (!customElements.get('sunbird-video-player')) {
                class SunbirdVideoPlayerFallback extends HTMLElement {
                    connectedCallback() {
                        var confAttr = this.getAttribute('player-config');
                        var cfg = {};
                        try {
                            cfg = JSON.parse(confAttr || '{}');
                        } catch(e) {}
                        
                        var artifact = (cfg.metadata && cfg.metadata.artifactUrl) || "${config.artifactUrl}";
                        var title = (cfg.metadata && cfg.metadata.name) || "${config.title}";

                        this.innerHTML = '<div style="width:100%; height:100%; background:#000; position:relative;">' +
                            '<video id="video-player_html5_api" class="vjs-tech" controls autoplay playsinline style="width:100%; height:100%; object-fit:contain;" src="' + artifact + '"></video>' +
                            '<div style="position:absolute; bottom:60px; left:12px; color:#fff; font-size:12px; background:rgba(0,0,0,0.6); padding:4px 8px; border-radius:4px;">' + title + '</div>' +
                            '</div>';
                        
                        // Fire mounted event
                        var v = this.querySelector('video');
                        if (v) {
                            v.playbackRate = TARGET_SPEED;
                            v.addEventListener('play', function() { emitPlayerEvent('PLAY'); });
                            v.addEventListener('pause', function() { emitPlayerEvent('PAUSE'); });
                            v.addEventListener('ratechange', function() {
                                notifySpeedChange(v.playbackRate);
                            });
                        }
                    }
                }
                customElements.define('sunbird-video-player', SunbirdVideoPlayerFallback);
            }

            function emitPlayerEvent(eventType, detail) {
                if (window.SunbirdBridge && window.SunbirdBridge.onPlayerEvent) {
                    window.SunbirdBridge.onPlayerEvent(JSON.stringify({
                        event: eventType,
                        detail: detail || {},
                        time: Date.now()
                    }));
                }
            }

            function emitTelemetryEvent(telemetryData) {
                if (window.SunbirdBridge && window.SunbirdBridge.onTelemetryEvent) {
                    window.SunbirdBridge.onTelemetryEvent(JSON.stringify(telemetryData));
                }
            }

            function notifySpeedChange(actualRate) {
                if (window.SunbirdBridge && window.SunbirdBridge.onSpeedVerification) {
                    window.SunbirdBridge.onSpeedVerification(JSON.stringify({
                        requested: TARGET_SPEED,
                        actual: actualRate,
                        success: Math.abs(actualRate - TARGET_SPEED) < 0.01,
                        timestamp: Date.now()
                    }));
                }
            }

            // Attach event listeners to Sunbird custom web component element
            if (playerElement) {
                playerElement.addEventListener('playerEvent', function(event) {
                    emitPlayerEvent(event.detail ? event.detail.type || 'PLAYER_EVENT' : 'EVENT', event.detail);
                });

                playerElement.addEventListener('telemetryEvent', function(event) {
                    emitTelemetryEvent(event.detail);
                });
            }

            // Core speed adjuster engine for Sunbird & Video.js
            window.setSunbirdPlaybackSpeed = function(speed) {
                TARGET_SPEED = speed;
                var result = {
                    requested: speed,
                    actual: 1.0,
                    success: false,
                    method: '',
                    videoId: '',
                    vjsPlayerId: '',
                    timestamp: Date.now()
                };

                // Method 1: Check Video.js player instances (Sunbird uses Video.js internally)
                var vjsPlayers = (typeof videojs !== 'undefined' && videojs.getAllPlayers) ? videojs.getAllPlayers() : [];
                if (vjsPlayers.length > 0) {
                    var player = vjsPlayers[0];
                    if (player && typeof player.playbackRate === 'function') {
                        player.playbackRate(speed);
                        result.vjsPlayerId = player.id_ || player.id || 'videojs-0';
                        result.method = 'Video.js player API [player.playbackRate(' + speed + ')]';
                    }
                }

                // Method 2: Access underlying HTML5 <video> element directly
                var videoElem = document.querySelector('video') || 
                               (playerElement && playerElement.querySelector && playerElement.querySelector('video')) ||
                               (playerElement && playerElement.shadowRoot && playerElement.shadowRoot.querySelector('video'));

                if (videoElem) {
                    videoElem.playbackRate = speed;
                    videoElem.defaultPlaybackRate = speed;
                    result.actual = videoElem.playbackRate;
                    result.videoId = videoElem.id || videoElem.className || 'HTML5 <video>';
                    if (!result.method) {
                        result.method = 'Direct HTML5 <video>.playbackRate = ' + speed;
                    } else {
                        result.method += ' + Direct <video>.playbackRate';
                    }
                } else if (vjsPlayers.length > 0 && vjsPlayers[0].playbackRate) {
                    result.actual = vjsPlayers[0].playbackRate();
                }

                result.success = Math.abs(result.actual - speed) < 0.01;

                if (window.SunbirdBridge && window.SunbirdBridge.onSpeedVerification) {
                    window.SunbirdBridge.onSpeedVerification(JSON.stringify(result));
                }

                return JSON.stringify(result);
            };

            // Diagnostics Inspector
            window.getSunbirdDiagnostics = function() {
                var vjsFound = typeof videojs !== 'undefined';
                var vjsPlayers = (vjsFound && videojs.getAllPlayers) ? videojs.getAllPlayers() : [];
                var videoElem = document.querySelector('video') || 
                               (playerElement && playerElement.querySelector && playerElement.querySelector('video')) ||
                               (playerElement && playerElement.shadowRoot && playerElement.shadowRoot.querySelector('video'));

                var actualRate = videoElem ? videoElem.playbackRate : (vjsPlayers.length > 0 ? vjsPlayers[0].playbackRate() : 1.0);
                var isPlaying = videoElem ? (!videoElem.paused && !videoElem.ended && videoElem.readyState > 2) : false;

                var diag = {
                    customElementRegistered: typeof customElements !== 'undefined' && !!customElements.get('sunbird-video-player'),
                    playerElementFound: !!playerElement,
                    underlyingVideoFound: !!videoElem,
                    videoElementId: videoElem ? (videoElem.id || videoElem.className || '<video>') : 'None',
                    videoJsInstanceFound: vjsPlayers.length > 0,
                    videoJsPlayerId: vjsPlayers.length > 0 ? (vjsPlayers[0].id_ || vjsPlayers[0].id || 'vjs') : 'None',
                    isPlaying: isPlaying,
                    currentTime: videoElem ? videoElem.currentTime : 0,
                    duration: videoElem ? videoElem.duration : 0,
                    requestedSpeed: TARGET_SPEED,
                    actualPlaybackRate: actualRate,
                    accessMethodUsed: videoElem ? 'HTML5 Video DOM element (id: ' + (videoElem.id || 'unnamed') + ')' : 'Scanning',
                    status: (videoElem && Math.abs(actualRate - TARGET_SPEED) < 0.01) ? 'SUCCESS' : 'PENDING'
                };

                if (window.SunbirdBridge && window.SunbirdBridge.onDiagnostics) {
                    window.SunbirdBridge.onDiagnostics(JSON.stringify(diag));
                }

                return JSON.stringify(diag);
            };

            // Initial poll
            setTimeout(function() {
                window.setSunbirdPlaybackSpeed(TARGET_SPEED);
                window.getSunbirdDiagnostics();
            }, 1000);

            // Repeat check after 2.5s once video metadata loads
            setTimeout(function() {
                window.setSunbirdPlaybackSpeed(TARGET_SPEED);
                window.getSunbirdDiagnostics();
            }, 2500);

        })();
    </script>
</body>
</html>
        """.trimIndent()
    }
}
