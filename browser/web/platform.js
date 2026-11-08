/* Browser platform glue. The game, layout, renderer, and protocol remain shared Java. */
(() => {
    'use strict';
    const MIN_WIDTH = 765, MIN_HEIGHT = 503;
    let WIDTH = MIN_WIDTH, HEIGHT = MIN_HEIGHT, PIXELS = WIDTH * HEIGHT;
    let resizable = false, laidOut = false;
    let requestedResizable = new URLSearchParams(location.search).get('mode') !== 'fixed';
    let notifiedWidth = -1, notifiedHeight = -1;
    const canvas = document.getElementById('game');
    const container = document.getElementById('game-container');
    const littleEndian = new Uint8Array(new Uint32Array([0x01020304]).buffer)[0] === 4;
    let input;
    let bound = false;
    let latestPixels;
    let presentationPending = false;
    let presentationRequest = 0;
    let context;
    let image;
    let destination;
    let pacing = false;
    let pacingRequest = 0;
    let framePermit = false;
    let frameTime = 0;
    let pacingFps = 0;
    let nextFrameTime = 0;
    let waiting;
    let workTimer = 0;
    let waitTicket = 0;
    let displayRatio;
    let displayViewportWidth;
    let displayViewportHeight;
    let displayRequest = 0;
    let displayRatioQuery;
    let audioResume;
    let audioResumeStarted = 0;
    let audioPrimed;
    let audioNeedsRecovery = false;
    let audioError = '';
    let audioEffects = [];

    function availableWidth() {
        const stage = document.getElementById('game-stage');
        const bounds = resizable && stage ? stage.getBoundingClientRect() : null;
        return Math.max(MIN_WIDTH, Math.floor(bounds?.width || window.innerWidth || MIN_WIDTH));
    }

    function availableHeight() {
        const stage = document.getElementById('game-stage');
        const bounds = stage?.getBoundingClientRect();
        const height = resizable && bounds?.height > 0 ? bounds.height
            : (window.innerHeight || MIN_HEIGHT) - Math.max(0, bounds?.top || 25);
        return Math.max(MIN_HEIGHT, Math.floor(height));
    }

    function surfaceSize(width, height) {
        if (WIDTH === width && HEIGHT === height) return false;
        WIDTH = width; HEIGHT = height; PIXELS = width * height;
        canvas.width = width; canvas.height = height;
        image = destination = context = latestPixels = undefined;
        if (presentationRequest) cancelAnimationFrame(presentationRequest);
        presentationRequest = 0; presentationPending = false;
        return true;
    }

    /**
     * Uses real viewport pixels in resizable mode; fixed mode keeps its original physical-pixel size.
     * Browser zoom is reflected in the measured rectangle used by input mapping.
     */
    function layoutDisplay(force = false) {
        document.querySelector('.client-shell')?.classList.toggle('resizable-client', resizable);
        const reportedRatio = window.devicePixelRatio;
        const ratio = Number.isFinite(reportedRatio) && reportedRatio > 0 ? reportedRatio : 1;
        const viewportWidth = resizable ? availableWidth() : window.innerWidth;
        const viewportHeight = resizable ? availableHeight() : window.innerHeight;
        if (!force && displayRatio === ratio && displayViewportWidth === viewportWidth
                && displayViewportHeight === viewportHeight) return;

        const ratioChanged = displayRatio !== ratio;
        displayRatio = ratio;
        displayViewportWidth = viewportWidth;
        displayViewportHeight = viewportHeight;
        canvas.style.width = container.style.width = WIDTH / (resizable ? 1 : ratio) + 'px';
        canvas.style.height = container.style.height = HEIGHT / (resizable ? 1 : ratio) + 'px';
        canvas.style.transform = 'none';
        const bounds = canvas.getBoundingClientRect();
        const snapX = (Math.round(bounds.left * ratio) - bounds.left * ratio) / ratio;
        const snapY = (Math.round(bounds.top * ratio) - bounds.top * ratio) / ratio;
        canvas.style.transform = 'translate(' + snapX + 'px,' + snapY + 'px)';

        if (resizable && (notifiedWidth !== viewportWidth || notifiedHeight !== viewportHeight)) {
            notifiedWidth = viewportWidth; notifiedHeight = viewportHeight;
            send(7, viewportWidth, viewportHeight);
        }
        if (ratioChanged && typeof window.matchMedia === 'function') {
            if (displayRatioQuery) {
                if (displayRatioQuery.removeEventListener) displayRatioQuery.removeEventListener('change', requestDisplayLayout);
                else displayRatioQuery.removeListener(requestDisplayLayout);
            }
            displayRatioQuery = window.matchMedia('(resolution: ' + ratio + 'dppx)');
            if (displayRatioQuery.addEventListener) displayRatioQuery.addEventListener('change', requestDisplayLayout);
            else displayRatioQuery.addListener(requestDisplayLayout);
        }
    }

    /**
     * Coalesces display-only resize notifications into one layout update, independent of the game frame loop.
     */
    function requestDisplayLayout() {
        if (displayRequest) return;
        displayRequest = requestAnimationFrame(() => {
            displayRequest = 0;
            layoutDisplay();
        });
    }

    function point(event) {
        const rect = canvas.getBoundingClientRect();
        return [Math.floor((event.clientX - rect.left) * WIDTH / rect.width),
            Math.floor((event.clientY - rect.top) * HEIGHT / rect.height)];
    }

    function send(type, x = 0, y = 0, key = 0, character = 0) {
        if (input) input(type, x, y, key, character);
    }

    function key(event, pressed) {
        // Keep browser shortcuts available while preserving the client's own arrow-key handling.
        if (event.metaKey || event.ctrlKey
                && !['Control', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'].includes(event.key)) return;
        let code = event.keyCode;
        if (code === 13) code = 10;
        if (code === 46) code = 127;
        const character = event.key.length === 1 ? event.key.charCodeAt(0) : 0;
        if (pressed) window.LunaPerformance?.input(event.timeStamp, 4);
        send(pressed ? 4 : 5, 0, 0, code, character);
        if (event.key !== 'F11') event.preventDefault();
    }

    function presentFrame() {
        presentationPending = false;
        presentationRequest = 0;
        const metrics = window.LunaPerformance;
        const profiling = !!metrics?.enabled;
        const start = profiling ? performance.now() : 0;
        if (!context) {
            context = canvas.getContext('2d', {alpha: false});
            image = context.createImageData(WIDTH, HEIGHT);
            destination = new Uint32Array(image.data.buffer, image.data.byteOffset, PIXELS);
        }
        const pixels = latestPixels;
        if (!pixels || pixels.length !== PIXELS) return;
        // Java stores RGB words; ImageData stores RGBA bytes. The final client framebuffer is opaque.
        if (littleEndian) {
            for (let index = 0; index < PIXELS; index++) {
                const rgb = pixels[index];
                destination[index] = 0xff000000 | (rgb & 255) << 16 | rgb & 0xff00 | rgb >>> 16 & 255;
            }
        } else {
            for (let index = 0; index < PIXELS; index++) destination[index] = pixels[index] << 8 | 255;
        }
        const conversionEnd = profiling ? performance.now() : 0;
        context.putImageData(image, 0, 0);
        if (profiling) metrics.present(start, conversionEnd, performance.now());
    }

    function wakeWaiting() {
        if (!waiting) return;
        const callback = waiting;
        waiting = undefined;
        if (workTimer) clearTimeout(workTimer);
        workTimer = 0;
        callback();
    }

    function workDeadline(ticket) {
        if (ticket === waitTicket) wakeWaiting();
    }

    function animationFrame(timestamp) {
        if (!pacing) return;
        pacingRequest = requestAnimationFrame(animationFrame);
        frameTime = timestamp;
        framePermit = true;
        wakeWaiting();
    }

    function stopEffects() {
        audioEffects.length = 0;
        window._lunaSoundGeneration = (window._lunaSoundGeneration || 0) + 1;
        if (window._lunaSounds) {
            for (const sound of window._lunaSounds) {
                try { sound.stop(); } catch { /* The sound may already have ended. */ }
                sound.disconnect();
            }
            window._lunaSounds.clear();
        }
    }

    function reportAudio(error) {
        const message = String(error?.message || error);
        if (message !== audioError) console.warn('Browser audio: ' + message);
        audioError = message;
    }

    function decodeEffect(effect) {
        const audio = effect.audio;
        audio.decodeAudioData(effect.bytes).then(buffer => {
            if (document.hidden || audio !== window._lunaAudio || audio.state !== 'running'
                    || effect.generation !== (window._lunaSoundGeneration || 0)
                    || performance.now() - effect.created > 500) return;
            if (!window._lunaGain) {
                window._lunaGain = audio.createGain();
                window._lunaGain.gain.value = typeof window._lunaVolume === 'number' ? window._lunaVolume : 1;
                window._lunaGain.connect(audio.destination);
            }
            const sounds = window._lunaSounds || (window._lunaSounds = new Set());
            const source = audio.createBufferSource();
            source.buffer = buffer;
            source.connect(window._lunaGain);
            if (sounds.size >= 16) {
                const oldest = sounds.values().next().value;
                sounds.delete(oldest);
                try { oldest.stop(); } catch { /* Already ended. */ }
                oldest.disconnect();
            }
            sounds.add(source);
            source.onended = () => { sounds.delete(source); source.disconnect(); };
            try { source.start(); }
            catch (error) { sounds.delete(source); source.disconnect(); throw error; }
        }).catch(reportAudio);
    }

    function readyAudio(audio, notifyMidi = true) {
        if (document.hidden || window._lunaAudio !== audio || audio.state !== 'running') return;
        audioError = '';
        if (notifyMidi) {
            try { window.LunaMidi?.contextReady?.(); } catch (error) { reportAudio(error); }
        }
        const waiting = audioEffects;
        audioEffects = [];
        for (const effect of waiting) {
            if (effect.audio === audio && performance.now() - effect.created <= 500
                    && effect.generation === (window._lunaSoundGeneration || 0)) decodeEffect(effect);
        }
    }

    function unlockAudioContext() {
        if (document.hidden) return;
        const AudioContext = window.AudioContext || window.webkitAudioContext;
        if (!AudioContext) return;
        try {
            try {
                if (navigator.audioSession && navigator.audioSession.type !== 'playback')
                    navigator.audioSession.type = 'playback';
            } catch { /* Audio Session is optional; ordinary Web Audio remains available. */ }
            let audio = window._lunaAudio;
            const stalledResume = audioResume && performance.now() - audioResumeStarted > 1500;
            if (!audio || audio.state === 'closed' || audioNeedsRecovery || stalledResume) {
                stopEffects();
                if (window._lunaGain) window._lunaGain.disconnect();
                window._lunaGain = undefined;
                if (audio && audio.state !== 'closed') Promise.resolve(audio.close()).catch(() => {});
                audio = window._lunaAudio = new AudioContext();
                audioPrimed = undefined;
                audioResume = undefined;
                audioNeedsRecovery = false;
                audio.addEventListener('statechange', () => {
                    if (window._lunaAudio !== audio) return;
                    if (audio.state === 'running') readyAudio(audio, false);
                    else stopEffects();
                });
            }
            // Start one silent sample in the same user gesture, including on older phone audio engines.
            if (audioPrimed !== audio) {
                const silent = audio.createBufferSource();
                silent.buffer = audio.createBuffer(1, 1, audio.sampleRate);
                silent.connect(audio.destination);
                silent.onended = () => silent.disconnect();
                silent.start();
                audioPrimed = audio;
            }
            if (audio.state === 'running') { readyAudio(audio); return; }
            // Retry on a later trusted gesture even when an earlier Safari resume promise stayed pending.
            if (!audioResume) audioResumeStarted = performance.now();
            const pending = Promise.resolve(audio.resume());
            audioResume = pending;
            pending.then(() => {
                if (window._lunaAudio !== audio) return;
                if (audio.state !== 'running') { audioNeedsRecovery = true; return; }
                readyAudio(audio);
            }).catch(reportAudio).finally(() => {
                if (audioResume === pending) { audioResume = undefined; audioResumeStarted = 0; }
            });
        } catch (error) { reportAudio(error); }
    }

    function playEffect(data) {
        const audio = window._lunaAudio;
        if (!audio || document.hidden || audio.state === 'closed') return false;
        try {
            let bytes;
            if (ArrayBuffer.isView(data)) bytes = new Uint8Array(data.buffer, data.byteOffset, data.byteLength);
            else if (data instanceof ArrayBuffer) bytes = new Uint8Array(data);
            else if (Array.isArray(data)) {
                if (!data.length || data.length > 2 * 1024 * 1024) return false;
                bytes = new Uint8Array(data);
            } else return false;
            if (!bytes.length || bytes.length > 2 * 1024 * 1024) return false;
            const copy = new Uint8Array(bytes.length);
            copy.set(bytes);
            const effect = {audio, bytes: copy.buffer, generation: window._lunaSoundGeneration || 0, created: performance.now()};
            if (audio.state === 'running') decodeEffect(effect);
            else if (audioResume) {
                if (audioEffects.length >= 4) audioEffects.shift();
                audioEffects.push(effect);
            } else return false;
            return true;
        } catch (error) { reportAudio(error); return false; }
    }


    window.LunaBrowser = {
        startPacing(defaultPacing) {
            const selection = new URLSearchParams(location.search).get('framePacing');
            if (selection === 'timer' || selection !== 'raf' && !defaultPacing) {
                if (pacing) window.LunaBrowser.stopPacing();
                return false;
            }
            if (pacing) return true;
            pacing = true;
            framePermit = false;
            pacingFps = 0;
            nextFrameTime = 0;
            if (presentationPending) {
                cancelAnimationFrame(presentationRequest);
                presentationPending = false;
                presentationRequest = 0;
            }
            pacingRequest = requestAnimationFrame(animationFrame);
            return true;
        },
        takeFrame(fps) {
            if (!pacing || !framePermit) return false;
            framePermit = false;
            if (document.hidden) return false;
            if (!Number.isFinite(fps) || fps < 1 || fps > 1000) {
                throw new RangeError('Invalid browser frame rate');
            }
            if (pacingFps !== fps) {
                pacingFps = fps;
                nextFrameTime = frameTime;
            }
            const period = 1000 / fps;
            const epsilon = 0.25;
            if (frameTime + epsilon < nextFrameTime) return false;
            const elapsedPeriods = Math.max(1, Math.floor((frameTime + epsilon - nextFrameTime) / period) + 1);
            nextFrameTime += elapsedPeriods * period;
            return true;
        },
        waitForWork(delayMillis, callback) {
            if (waiting) throw new Error('A browser frame wait is already pending');
            if (typeof callback !== 'function') throw new TypeError('A browser frame wait requires a callback');
            waiting = callback;
            const delay = Number.isFinite(delayMillis) ? Math.max(1, Math.min(20, delayMillis)) : 20;
            workTimer = setTimeout(workDeadline, delay, ++waitTicket);
        },
        stopPacing() {
            pacing = false;
            framePermit = false;
            if (pacingRequest) cancelAnimationFrame(pacingRequest);
            pacingRequest = 0;
            wakeWaiting();
        },
        unlockAudio() { unlockAudioContext(); },
        playEffect,
        stopEffects,
        bind(width, height, handler) {
            input = handler;
            if (bound) return;
            bound = true;
            canvas.width = WIDTH;
            canvas.height = HEIGHT;
            canvas.addEventListener('contextmenu', event => event.preventDefault());
            canvas.addEventListener('mousemove', event => send(0, ...point(event)));
            canvas.addEventListener('mousedown', event => {
                if (event.button !== 0 && event.button !== 2) return;
                window.LunaPerformance?.input(event.timeStamp, 1);
                canvas.focus({preventScroll: true});
                send(1, ...point(event), event.button === 2 ? 2 : 1);
                event.preventDefault();
            });
            window.addEventListener('mouseup', () => send(2));
            canvas.addEventListener('mouseleave', () => send(6, -1, -1));
            canvas.addEventListener('blur', () => send(3));
            canvas.addEventListener('focus', () => send(10));
            window.addEventListener('blur', () => send(3));
            canvas.addEventListener('keydown', event => key(event, true));
            canvas.addEventListener('keyup', event => key(event, false));
            canvas.addEventListener('wheel', event => {
                if (event.ctrlKey || event.metaKey) return;
                const pixels = event.deltaY * (event.deltaMode === 1 ? 16 : event.deltaMode === 2 ? HEIGHT : 1);
                if (pixels !== 0) {
                    window.LunaPerformance?.input(event.timeStamp, 9);
                    const delta = Math.sign(pixels) * Math.max(1, Math.min(120, Math.round(Math.abs(pixels))));
                    send(9, ...point(event), 0, delta);
                }
                event.preventDefault();
            }, {passive: false});
            document.addEventListener('visibilitychange', () => send(document.hidden ? 3 : 10));
            send(8, requestedResizable ? 1 : 0);
        },
        initialMode() { return requestedResizable ? 1 : 0; },
        isResizable() { return resizable; },
        availableWidth,
        availableHeight,
        setClientMode(next) {
            requestedResizable = !!next;
            const url = new URL(location.href);
            if (requestedResizable) url.searchParams.delete('mode');
            else url.searchParams.set('mode', 'fixed');
            history.replaceState(history.state, '', url.href);
            send(8, requestedResizable ? 1 : 0);
            document.dispatchEvent(new Event('luna-client-mode'));
        },
        layout(nextResizable, width, height) {
            resizable = !!nextResizable;
            if (laidOut) requestedResizable = resizable;
            laidOut = true;
            surfaceSize(width, height);
            notifiedWidth = notifiedHeight = -1;
            layoutDisplay(true);
            document.dispatchEvent(new Event('luna-client-mode'));
        },
        present(pixels, width, height) {
            if (!Number.isInteger(width) || !Number.isInteger(height) || width < MIN_WIDTH || height < MIN_HEIGHT
                    || width > 32768 || height > 32768 || pixels.length !== width * height) {
                throw new RangeError('Invalid browser client framebuffer dimensions.');
            }
            if (surfaceSize(width, height)) layoutDisplay(true);
            latestPixels = pixels;
            if (pacing) {
                presentFrame();
                return;
            }
            if (presentationPending) return;
            presentationPending = true;
            presentationRequest = requestAnimationFrame(presentFrame);
        }
    };
    function unlockAudioFromGesture(event) {
        if (event.isTrusted) window.LunaBrowser.unlockAudio();
    }
    document.addEventListener('pointerdown', unlockAudioFromGesture, {capture: true, passive: true});
    document.addEventListener('touchstart', unlockAudioFromGesture, {capture: true, passive: true});
    document.addEventListener('keydown', unlockAudioFromGesture, {capture: true});
    for (const event of ['pointerup', 'touchend', 'click'])
        document.addEventListener(event, unlockAudioFromGesture, {capture: true, passive: true});
    const interrupted = () => {
        audioNeedsRecovery = !!window._lunaAudio;
        stopEffects();
    };
    window.addEventListener('blur', interrupted);
    window.addEventListener('pagehide', interrupted);
    document.addEventListener('visibilitychange', () => { if (document.hidden) interrupted(); });
    window.addEventListener('resize', requestDisplayLayout);
    document.addEventListener('fullscreenchange', requestDisplayLayout);
    if (typeof ResizeObserver === 'function') {
        const stage = document.getElementById('game-stage');
        if (stage) new ResizeObserver(requestDisplayLayout).observe(stage);
    }
    layoutDisplay();
})();
