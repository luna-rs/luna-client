/* Browser platform glue. The game, layout, renderer, and protocol remain shared Java. */
(() => {
    'use strict';
    const canvas = document.getElementById('game');
    const container = document.getElementById('game-container');
    const mode = document.getElementById('display-mode');
    let input;
    let bound = false;
    let resizable = false;
    let latestFrame;
    let presentationPending = false;
    let lastWidth = 0;
    let lastHeight = 0;
    let resizeTimer;

    function point(event) {
        const rect = canvas.getBoundingClientRect();
        return [Math.floor((event.clientX - rect.left) * canvas.width / rect.width),
            Math.floor((event.clientY - rect.top) * canvas.height / rect.height)];
    }

    function send(type, x = 0, y = 0, key = 0, character = 0) {
        if (input) input(type, x, y, key, character);
    }

    function applyResize() {
        if (!resizable) return;
        const width = Math.max(765, Math.min(1920, Math.floor(container.clientWidth)));
        const height = Math.max(503, Math.min(1080, Math.floor(container.clientHeight)));
        if (width !== lastWidth || height !== lastHeight) {
            lastWidth = width;
            lastHeight = height;
            send(7, width, height);
        }
    }

    function resize() {
        if (resizeTimer !== undefined) clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
            resizeTimer = undefined;
            applyResize();
        }, 200);
    }

    function key(event, pressed) {
        if (pressed && event.key === 'Escape' && document.fullscreenElement) {
            document.exitFullscreen().catch(error => console.warn('Fullscreen: ' + error.message));
            return;
        }
        // Preserve browser zoom, reload, developer shortcuts, and normal keyboard navigation out of the game.
        if (event.metaKey || event.ctrlKey && !['Control', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'].includes(event.key)) return;
        let code = event.keyCode;
        if (code === 13) code = 10;
        if (code === 46) code = 127;
        const character = event.key.length === 1 ? event.key.charCodeAt(0) : 0;
        send(pressed ? 4 : 5, 0, 0, code, character);
        if (event.key !== 'F11') event.preventDefault();
    }

    function updateSizeLabel(width, height) {
        const label = document.getElementById('viewport-size');
        if (label) label.textContent = (resizable ? 'Resizable' : 'Fixed') + ' · ' + width + ' × ' + height;
    }

    window.LunaBrowser = {
        unlockAudio() {
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            if (!AudioContext) return;
            try {
                if (!window._lunaAudio) window._lunaAudio = new AudioContext();
                if (window._lunaAudio.state === 'suspended') window._lunaAudio.resume().catch(error => console.warn('Audio: ' + error.message));
            } catch (error) {
                console.warn('Audio: ' + error.message);
            }
        },
        bind(width, height, handler) {
            input = handler;
            if (bound) return;
            bound = true;
            canvas.width = width;
            canvas.height = height;
            canvas.addEventListener('contextmenu', event => event.preventDefault());
            canvas.addEventListener('mousemove', event => send(0, ...point(event)));
            canvas.addEventListener('mousedown', event => {
                if (event.button !== 0 && event.button !== 2) return;
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
                const pixels = event.deltaY * (event.deltaMode === 1 ? 16 : event.deltaMode === 2 ? canvas.height : 1);
                if (pixels !== 0) send(9, ...point(event), 0, Math.sign(pixels) * Math.max(1, Math.min(120, Math.round(Math.abs(pixels)))));
                event.preventDefault();
            }, {passive: false});
            document.addEventListener('visibilitychange', () => send(document.hidden ? 3 : 10));
            mode.addEventListener('change', () => {
                send(8, mode.value === 'resizable' ? 1 : 0);
                canvas.focus({preventScroll: true});
            });
            new ResizeObserver(resize).observe(container);
            window.addEventListener('resize', resize);
            document.addEventListener('fullscreenchange', resize);
            send(8, mode.value === 'resizable' ? 1 : 0);
        },
        layout(nextResizable, width, height) {
            resizable = nextResizable;
            mode.value = resizable ? 'resizable' : 'fixed';
            container.classList.toggle('resizable', resizable);
            canvas.style.width = width + 'px';
            canvas.style.height = height + 'px';
            updateSizeLabel(width, height);
            applyResize();
        },
        present(pixels, width, height) {
            latestFrame = {pixels, width, height};
            if (presentationPending) return;
            presentationPending = true;
            requestAnimationFrame(() => {
                presentationPending = false;
                const frame = latestFrame;
                if (canvas.width !== frame.width) canvas.width = frame.width;
                if (canvas.height !== frame.height) canvas.height = frame.height;
                canvas.style.width = frame.width + 'px';
                canvas.style.height = frame.height + 'px';
                const context = canvas.getContext('2d', {alpha: false});
                let image = canvas._lunaFrame;
                if (!image || image.width !== frame.width || image.height !== frame.height)
                    image = canvas._lunaFrame = context.createImageData(frame.width, frame.height);
                const destination = image.data;
                for (let index = 0, offset = 0; index < frame.pixels.length; index++, offset += 4) {
                    const rgb = frame.pixels[index];
                    destination[offset] = rgb >>> 16 & 255;
                    destination[offset + 1] = rgb >>> 8 & 255;
                    destination[offset + 2] = rgb & 255;
                    destination[offset + 3] = 255;
                }
                context.putImageData(image, 0, 0);
                updateSizeLabel(frame.width, frame.height);
            });
        }
    };

    document.getElementById('fullscreen').addEventListener('click', async () => {
        try {
            if (document.fullscreenElement) await document.exitFullscreen();
            else {
                mode.value = 'resizable';
                send(8, 1);
                await container.requestFullscreen();
            }
            canvas.focus({preventScroll: true});
        } catch (error) {
            document.getElementById('status').textContent = 'Fullscreen unavailable: ' + error.message;
        }
    });
    const exitFullscreen = document.getElementById('exit-fullscreen');
    if (exitFullscreen) exitFullscreen.addEventListener('click', () => {
        document.exitFullscreen().catch(error => console.warn('Fullscreen: ' + error.message));
    });
    if (!container.requestFullscreen) document.getElementById('fullscreen').disabled = true;
})();
