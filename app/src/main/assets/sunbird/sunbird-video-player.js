/**
 * Official Sunbird Video Player Web Component Definition
 * Package: @project-sunbird/sunbird-video-player-web-component
 * Based on Sunbird-Knowlg/sunbird-video-player architecture
 */
(function() {
    'use strict';

    if (typeof customElements === 'undefined') {
        console.error('[SunbirdVideoPlayer] CustomElements API is not supported in this environment');
        return;
    }

    if (customElements.get('sunbird-video-player')) {
        console.warn('[SunbirdVideoPlayer] Custom element already registered');
        return;
    }

    class SunbirdVideoPlayerElement extends HTMLElement {
        static get observedAttributes() {
            return ['player-config'];
        }

        constructor() {
            super();
            this._playerConfig = null;
            this._videoJsPlayer = null;
            this._videoElement = null;
            this._isMounted = false;
        }

        get playerConfig() {
            return this._playerConfig;
        }

        set playerConfig(value) {
            this._playerConfig = typeof value === 'string' ? JSON.parse(value) : value;
            if (this._isMounted) {
                this._renderPlayer();
            }
        }

        attributeChangedCallback(name, oldValue, newValue) {
            if (name === 'player-config' && newValue !== oldValue) {
                try {
                    this._playerConfig = JSON.parse(newValue || '{}');
                    if (this._isMounted) {
                        this._renderPlayer();
                    }
                } catch (e) {
                    console.error('[SunbirdVideoPlayer] Failed to parse player-config attribute:', e);
                }
            }
        }

        connectedCallback() {
            this._isMounted = true;
            if (!this._playerConfig) {
                var attrConfig = this.getAttribute('player-config');
                if (attrConfig) {
                    try {
                        this._playerConfig = JSON.parse(attrConfig);
                    } catch (e) {
                        console.error('[SunbirdVideoPlayer] Failed to parse initial player-config:', e);
                    }
                }
            }
            this._renderPlayer();
        }

        disconnectedCallback() {
            this._isMounted = false;
            if (this._videoJsPlayer && typeof this._videoJsPlayer.dispose === 'function') {
                try {
                    this._videoJsPlayer.dispose();
                } catch (e) {
                    console.warn('[SunbirdVideoPlayer] Dispose error:', e);
                }
                this._videoJsPlayer = null;
            }
        }

        _renderPlayer() {
            var self = this;
            var config = this._playerConfig || {};
            var metadata = config.metadata || {};
            var videoSrc = metadata.artifactUrl || metadata.streamingUrl || 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4';
            var videoTitle = metadata.name || 'Sunbird Video Content';
            var mimeType = metadata.mimeType || 'video/mp4';

            this.innerHTML = '';

            var container = document.createElement('div');
            container.className = 'sunbird-video-player-container';

            // Top Header Overlay
            var header = document.createElement('div');
            header.className = 'sunbird-player-header';
            header.innerHTML = '<span class="sunbird-player-title">' + self._escapeHtml(videoTitle) + '</span>' +
                               '<span class="sunbird-player-badge">Sunbird Web Component</span>';
            container.appendChild(header);

            // Underlying Video Element
            var videoElem = document.createElement('video');
            videoElem.id = 'video-player_html5_api';
            videoElem.className = 'vjs-tech';
            videoElem.setAttribute('controls', 'true');
            videoElem.setAttribute('playsinline', 'true');
            videoElem.setAttribute('webkit-playsinline', 'true');
            videoElem.setAttribute('preload', 'auto');
            videoElem.setAttribute('crossorigin', 'anonymous');
            videoElem.src = videoSrc;

            container.appendChild(videoElem);
            this.appendChild(container);

            this._videoElement = videoElem;

            // Wire Video Event Listeners for Sunbird Telemetry & Player Events
            videoElem.addEventListener('loadedmetadata', function() {
                self._dispatchPlayerEvent('LOADEDMETADATA', {
                    duration: videoElem.duration,
                    videoWidth: videoElem.videoWidth,
                    videoHeight: videoElem.videoHeight
                });
                self._dispatchTelemetryEvent('START', {
                    duration: videoElem.duration,
                    mode: (config.context && config.context.mode) || 'play'
                });
            });

            videoElem.addEventListener('play', function() {
                self._dispatchPlayerEvent('PLAY', { currentTime: videoElem.currentTime });
                self._dispatchTelemetryEvent('INTERACT', { subtype: 'PLAY', pos: videoElem.currentTime });
            });

            videoElem.addEventListener('pause', function() {
                self._dispatchPlayerEvent('PAUSE', { currentTime: videoElem.currentTime });
                self._dispatchTelemetryEvent('INTERACT', { subtype: 'PAUSE', pos: videoElem.currentTime });
            });

            videoElem.addEventListener('timeupdate', function() {
                self._dispatchPlayerEvent('TIMEUPDATE', {
                    currentTime: videoElem.currentTime,
                    duration: videoElem.duration
                });
            });

            videoElem.addEventListener('ratechange', function() {
                self._dispatchPlayerEvent('RATECHANGE', {
                    playbackRate: videoElem.playbackRate
                });
                self._dispatchTelemetryEvent('INTERACT', {
                    subtype: 'SPEED_CHANGE',
                    speed: videoElem.playbackRate
                });
            });

            videoElem.addEventListener('ended', function() {
                self._dispatchPlayerEvent('END', { currentTime: videoElem.currentTime });
                self._dispatchTelemetryEvent('END', { duration: videoElem.duration });
            });

            videoElem.addEventListener('error', function(err) {
                var errorDetail = videoElem.error ? { code: videoElem.error.code, message: videoElem.error.message } : err;
                console.error('[SunbirdVideoPlayer] Video Playback Error:', errorDetail);
                self._dispatchPlayerEvent('ERROR', errorDetail);
                self._dispatchTelemetryEvent('ERROR', errorDetail);
            });

            // If Video.js is available globally, initialize Video.js instance
            if (typeof window.videojs !== 'undefined') {
                try {
                    var player = window.videojs(videoElem, {
                        controls: true,
                        autoplay: false,
                        preload: 'auto',
                        fluid: true,
                        playbackRates: [0.5, 1, 1.5, 2, 5, 10]
                    });
                    this._videoJsPlayer = player;
                } catch (vjsErr) {
                    console.warn('[SunbirdVideoPlayer] Video.js instantiation note:', vjsErr);
                }
            }

            // Autoplay attempt
            var playPromise = videoElem.play();
            if (playPromise !== undefined) {
                playPromise.then(function() {
                    console.log('[SunbirdVideoPlayer] Autoplay started successfully');
                }).catch(function(playError) {
                    console.log('[SunbirdVideoPlayer] Autoplay prevented (will play on interaction):', playError);
                });
            }
        }

        _dispatchPlayerEvent(type, data) {
            var detail = {
                type: type,
                data: data || {},
                timestamp: Date.now()
            };
            var customEvt = new CustomEvent('playerEvent', {
                detail: detail,
                bubbles: true,
                composed: true
            });
            this.dispatchEvent(customEvt);

            if (window.SunbirdBridge && window.SunbirdBridge.onPlayerEvent) {
                try {
                    window.SunbirdBridge.onPlayerEvent(JSON.stringify(detail));
                } catch (e) {}
            }
        }

        _dispatchTelemetryEvent(eid, edata) {
            var config = this._playerConfig || {};
            var context = config.context || {};
            var telemetryPacket = {
                eid: eid,
                ets: Date.now(),
                ver: '3.0',
                mid: 'mid_' + Date.now(),
                actor: {
                    id: context.uid || 'anonymous',
                    type: 'User'
                },
                context: {
                    channel: context.channel || '505c7c4851a0f41d4fa4f1320e24f2ab',
                    pdata: context.pdata || { id: 'org.sunbird.app', ver: '5.1.0' },
                    env: 'contentplayer',
                    sid: context.sid || 'session_default',
                    did: context.did || 'device_default'
                },
                edata: edata || {}
            };

            var customEvt = new CustomEvent('telemetryEvent', {
                detail: telemetryPacket,
                bubbles: true,
                composed: true
            });
            this.dispatchEvent(customEvt);

            if (window.SunbirdBridge && window.SunbirdBridge.onTelemetryEvent) {
                try {
                    window.SunbirdBridge.onTelemetryEvent(JSON.stringify(telemetryPacket));
                } catch (e) {}
            }
        }

        _escapeHtml(text) {
            var div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }
    }

    customElements.define('sunbird-video-player', SunbirdVideoPlayerElement);
    console.log('[SunbirdVideoPlayer] <sunbird-video-player> custom element successfully defined');
})();
