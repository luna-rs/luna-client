/* Browser platform glue. The game, layout, renderer, and protocol remain shared Java. */
(() => {
    'use strict';
    const WIDTH = 765;
    const HEIGHT = 503;
    const PIXELS = WIDTH * HEIGHT;
    const canvas = document.getElementById('game');
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
        unlockAudio() {
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            if (!AudioContext) return;
            try {
                if (!window._lunaAudio) window._lunaAudio = new AudioContext();
                if (window._lunaAudio.state === 'suspended') {
                    window._lunaAudio.resume().catch(error => console.warn('Audio: ' + error.message));
                }
            } catch (error) {
                console.warn('Audio: ' + error.message);
            }
        },
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
            send(8, 0);
        },
        layout(resizable, width, height) {
            canvas.style.width = WIDTH + 'px';
            canvas.style.height = HEIGHT + 'px';
            if (resizable) send(8, 0);
        },
        present(pixels, width, height) {
            if (width !== WIDTH || height !== HEIGHT || pixels.length !== PIXELS) {
                throw new RangeError('The browser client framebuffer must remain 765 × 503.');
            }
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
})();
