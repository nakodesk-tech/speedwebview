package com.example.script

object DikshaScript {
    val INJECTION_SCRIPT: String = """
        (function() {
            if (window.__dikshaSpeedEngineInitialized) return;
            window.__dikshaSpeedEngineInitialized = true;
            window.__dikshaSpeedTarget = window.__dikshaSpeedTarget || 1.0;

            function findVideoInDocument(doc) {
                if (!doc) return null;
                try {
                    var videos = Array.from(doc.querySelectorAll('video'));
                    if (!videos || videos.length === 0) return null;
                    // 1. Prefer actively playing video
                    var playing = videos.find(function(v) { return !v.paused && !v.ended && v.readyState > 1; });
                    if (playing) return playing;
                    // 2. Prefer video that has loaded metadata / dimensions
                    var ready = videos.find(function(v) { return v.readyState > 0 || (v.videoWidth > 0 && v.videoHeight > 0) || v.duration > 0; });
                    if (ready) return ready;
                    // 3. Fallback to first video
                    return videos[0];
                } catch(e) {
                    return null;
                }
            }

            function getAllVideosInDocument(doc) {
                if (!doc) return [];
                try {
                    return Array.from(doc.querySelectorAll('video'));
                } catch(e) {
                    return [];
                }
            }

            window.dikshaSpeedFindActiveVideo = function() {
                var isCrossOriginIframe = false;
                // 1. Search main document
                var v = findVideoInDocument(document);
                var docVideos = getAllVideosInDocument(document);
                if (v) return { video: v, isCrossOrigin: false, totalVideos: docVideos.length };

                // 2. Check iframes (only where same-origin access is permitted)
                var iframes = Array.from(document.querySelectorAll('iframe'));
                var totalVideos = docVideos.length;
                for (var i = 0; i < iframes.length; i++) {
                    try {
                        var iframeDoc = iframes[i].contentDocument || (iframes[i].contentWindow && iframes[i].contentWindow.document);
                        if (iframeDoc) {
                            var iframeVideos = getAllVideosInDocument(iframeDoc);
                            totalVideos += iframeVideos.length;
                            var iv = findVideoInDocument(iframeDoc);
                            if (iv) return { video: iv, isCrossOrigin: false, totalVideos: totalVideos };
                        }
                    } catch (err) {
                        isCrossOriginIframe = true;
                    }
                }

                return { video: null, isCrossOrigin: isCrossOriginIframe, totalVideos: totalVideos };
            };

            window.dikshaSpeedSet = function(speed) {
                var numSpeed = parseFloat(speed);
                if (isNaN(numSpeed) || numSpeed <= 0) {
                    return JSON.stringify({ success: false, reason: "INVALID_SPEED", requested: speed, actual: 1.0 });
                }
                window.__dikshaSpeedTarget = numSpeed;
                var res = window.dikshaSpeedFindActiveVideo();
                if (!res.video) {
                    var reason = res.isCrossOrigin ? "CROSS_ORIGIN_IFRAME" : "NO_VIDEO";
                    var out = {
                        success: false,
                        reason: reason,
                        requested: numSpeed,
                        actual: 1.0,
                        videoCount: res.totalVideos,
                        message: res.isCrossOrigin ? "Video is inside a cross-origin iframe." : "No HTML video element found on page."
                    };
                    if (window.DikshaSpeedBridge && window.DikshaSpeedBridge.onSpeedChanged) {
                        window.DikshaSpeedBridge.onSpeedChanged(JSON.stringify(out));
                    }
                    return JSON.stringify(out);
                }

                var video = res.video;
                try {
                    video.playbackRate = numSpeed;
                    video.defaultPlaybackRate = numSpeed;

                    if (!video.__hasDikshaRateListener) {
                        video.__hasDikshaRateListener = true;
                        var maintainSpeed = function() {
                            if (window.__dikshaSpeedTarget && Math.abs(video.playbackRate - window.__dikshaSpeedTarget) > 0.01) {
                                video.playbackRate = window.__dikshaSpeedTarget;
                            }
                        };
                        video.addEventListener('play', maintainSpeed);
                        video.addEventListener('ratechange', maintainSpeed);
                        video.addEventListener('loadeddata', maintainSpeed);
                    }

                    var actualRate = video.playbackRate;
                    var isSuccess = Math.abs(actualRate - numSpeed) < 0.01;
                    var out = {
                        success: isSuccess,
                        requested: numSpeed,
                        actual: actualRate,
                        videoCount: Math.max(1, res.totalVideos),
                        isPlaying: !video.paused && !video.ended,
                        currentTime: video.currentTime || 0,
                        duration: video.duration || 0,
                        reason: isSuccess ? "SUCCESS" : "RATE_MISMATCH"
                    };

                    if (window.DikshaSpeedBridge && window.DikshaSpeedBridge.onSpeedChanged) {
                        window.DikshaSpeedBridge.onSpeedChanged(JSON.stringify(out));
                    }
                    return JSON.stringify(out);
                } catch (e) {
                    var out = {
                        success: false,
                        reason: "EXCEPTION: " + e.message,
                        requested: numSpeed,
                        actual: video.playbackRate || 1.0
                    };
                    if (window.DikshaSpeedBridge && window.DikshaSpeedBridge.onSpeedChanged) {
                        window.DikshaSpeedBridge.onSpeedChanged(JSON.stringify(out));
                    }
                    return JSON.stringify(out);
                }
            };

            window.dikshaSpeedGetDiagnostics = function() {
                var res = window.dikshaSpeedFindActiveVideo();
                var v = res.video;
                var target = window.__dikshaSpeedTarget || 1.0;
                var actual = v ? v.playbackRate : 1.0;
                var isMatched = Math.abs(actual - target) < 0.01;
                var statusStr = "NO_VIDEO";
                if (v) {
                    statusStr = isMatched ? "ACTIVE" : "RATE_MISMATCH";
                } else if (res.isCrossOrigin) {
                    statusStr = "CROSS_ORIGIN_IFRAME";
                }

                var out = {
                    url: window.location.href,
                    videoFound: !!v,
                    videoCount: res.totalVideos || (v ? 1 : 0),
                    activeVideo: !!v,
                    isPlaying: v ? (!v.paused && !v.ended) : false,
                    requestedSpeed: target,
                    actualPlaybackRate: actual,
                    isCrossOriginIframe: res.isCrossOrigin,
                    currentTime: v ? (v.currentTime || 0) : 0,
                    duration: v ? (v.duration || 0) : 0,
                    status: statusStr
                };
                if (window.DikshaSpeedBridge && window.DikshaSpeedBridge.onDiagnosticsUpdated) {
                    window.DikshaSpeedBridge.onDiagnosticsUpdated(JSON.stringify(out));
                }
                return JSON.stringify(out);
            };

            // Lightweight dynamic observer
            try {
                var observer = new MutationObserver(function(mutations) {
                    if (window.__dikshaSpeedTarget && window.__dikshaSpeedTarget !== 1.0) {
                        var res = window.dikshaSpeedFindActiveVideo();
                        if (res.video && Math.abs(res.video.playbackRate - window.__dikshaSpeedTarget) > 0.01) {
                            window.dikshaSpeedSet(window.__dikshaSpeedTarget);
                        }
                    }
                });
                observer.observe(document.documentElement || document.body, { childList: true, subtree: true });
            } catch(e) {}
        })();
    """.trimIndent()

    fun buildSetSpeedCall(speed: Double): String {
        return "window.dikshaSpeedSet ? window.dikshaSpeedSet($speed) : (function(){ $INJECTION_SCRIPT; return window.dikshaSpeedSet($speed); })();"
    }

    fun buildDiagnosticsCall(): String {
        return "window.dikshaSpeedGetDiagnostics ? window.dikshaSpeedGetDiagnostics() : (function(){ $INJECTION_SCRIPT; return window.dikshaSpeedGetDiagnostics(); })();"
    }
}
