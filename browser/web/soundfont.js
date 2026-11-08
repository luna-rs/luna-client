/**
 * Loads the desktop SoundFont lazily and renders it off the game thread.
 *
 * @author lare96
 */
(() => {
    'use strict';
    const MAX_BANK_BYTES = 16 * 1024 * 1024;
    let controller, bank;
    const modules = new WeakMap();

    function assetUrl(name) {
        return typeof window.LunaAssetUrl === 'function'
            ? window.LunaAssetUrl(name) : new URL(name, document.baseURI).href;
    }
    function bounded(operation, message) {
        return new Promise((resolve, reject) => {
            let settled = false;
            const finish = (success, value) => {
                if (settled) return;
                settled = true; clearTimeout(timer);
                if (success) resolve(value); else reject(value);
            };
            const timer = setTimeout(() => finish(false, Error(message)), 15000);
            operation.then(value => finish(true, value), error => finish(false, error));
        });
    }
    async function download(name, maximum) {
        const abort = new AbortController();
        const timer = setTimeout(() => abort.abort(), 15000);
        try {
            const response = await fetch(assetUrl(name), {cache: 'no-cache', signal: abort.signal});
            if (!response.ok) throw Error('SoundFont asset download failed: ' + name + ' (HTTP ' + response.status + ')');
            const advertised = Number(response.headers.get('content-length'));
            if (Number.isFinite(advertised) && advertised > maximum) throw Error('SoundFont asset exceeds its size limit');
            if (!response.body?.getReader) {
                const bytes = new Uint8Array(await response.arrayBuffer());
                if (bytes.length > maximum) throw Error('SoundFont asset exceeds its size limit');
                return bytes;
            }
            const reader = response.body.getReader(), chunks = [];
            let length = 0;
            try {
                for (;;) {
                    const next = await reader.read();
                    if (next.done) break;
                    length += next.value.length;
                    if (length > maximum) { abort.abort(); throw Error('SoundFont asset exceeds its size limit'); }
                    chunks.push(next.value);
                }
            } finally { reader.releaseLock(); }
            const bytes = new Uint8Array(length);
            let offset = 0;
            for (const chunk of chunks) { bytes.set(chunk, offset); offset += chunk.length; }
            return bytes;
        } finally { clearTimeout(timer); abort.abort(); }
    }
    function loadController() {
        if (!controller) {
            controller = bounded(import(assetUrl('audio/spessasynth-controller.js')), 'SoundFont controller load timed out')
                .catch(error => { controller = null; throw error; });
        }
        return controller;
    }
    function soundbank() {
        if (!bank) bank = bounded((async () => {
            const metadata = JSON.parse(new TextDecoder().decode(await download('audio/soundbank.json', 4096)));
            if (!metadata || !Number.isSafeInteger(metadata.size) || metadata.size <= 0 || metadata.size > MAX_BANK_BYTES
                    || typeof metadata.sha256 !== 'string' || !/^[a-f0-9]{64}$/.test(metadata.sha256))
                throw Error('Invalid SoundFont metadata');
            const bytes = await download('audio/soundbank.sf2', metadata.size);
            if (bytes.length !== metadata.size) throw Error('The SoundFont size differs from its build metadata');
            const digest = await crypto.subtle.digest('SHA-256', bytes);
            const hash = Array.from(new Uint8Array(digest), value => value.toString(16).padStart(2, '0')).join('');
            if (hash !== metadata.sha256) throw Error('The SoundFont checksum differs from its build metadata');
            return bytes;
        })(), 'SoundFont verification timed out').catch(error => { bank = null; throw error; });
        return bank;
    }
    function addWorklet(audio) {
        if (!modules.has(audio)) {
            const promise = bounded(Promise.resolve().then(() => audio.audioWorklet.addModule(assetUrl('audio/spessasynth-worklet.js'))),
                'SoundFont audio processor load timed out')
                .catch(error => { modules.delete(audio); throw error; });
            modules.set(audio, promise);
        }
        return modules.get(audio);
    }
    window.LunaSoundFont = {
        create(audio, destination) {
            if (!audio?.audioWorklet || audio.state === 'closed' || !destination)
                return Promise.reject(Error('SoundFont music requires a usable AudioWorklet context'));
            let synth, node, disposed = false, retired = false;
            const closePort = () => {
                if (retired) return;
                retired = true;
                audio.removeEventListener('statechange', onContextState);
                node?.removeEventListener('processorerror', onProcessorError);
                try { node?.port.close(); } catch {}
            };
            const onContextState = () => { if (audio.state === 'closed') closePort(); };
            const dispose = () => {
                if (disposed) return;
                disposed = true;
                // The worklet acknowledges disposal before closing its port, and returns false from process().
                // Keep a suspended context's message queued until it resumes, so a detached processor cannot revive.
                try { node?.port.postMessage({lunaMusic: 1, dispose: true}); } catch { closePort(); }
                try { synth?.destroy(); } catch {}
                try { node?.disconnect(); } catch {}
                if (!node || audio.state === 'closed') closePort();
            };
            const onProcessorError = () => { dispose(); closePort(); };
            const alive = () => {
                if (disposed || audio.state === 'closed') throw Error('SoundFont music initialization was cancelled');
            };
            const initialize = async () => {
                const [library, bytes] = await Promise.all([loadController(), soundbank(), addWorklet(audio)]);
                alive();
                synth = new library.WorkletSynthesizer(audio, {
                    eventsEnabled: false, convolverMode: false, decodedSampleBudget: 16 * 1024 * 1024,
                    audioNodeCreators: {worklet(context, name, options) {
                        node = new AudioWorkletNode(context, name, options);
                        node.addEventListener('processorerror', onProcessorError);
                        node.port.addEventListener('message', event => {
                            if (event.data?.lunaDisposed) { event.stopImmediatePropagation(); closePort(); }
                        });
                        node.port.start();
                        audio.addEventListener('statechange', onContextState);
                        return node;
                    }}
                });
                await synth.isReady; alive();
                await synth.soundBankManager.addSoundBank(bytes.slice().buffer, 'luna-desktop'); alive();
                synth.setLogLevel(false, true, false);
                synth.setSystemParameter('voiceCap', 64);
                synth.setSystemParameter('autoAllocateVoices', false);
                synth.setSystemParameter('interpolationType', 0);
                // Match the desktop SoftSynthesizer's level with this same bank; master retains its 3 dB headroom.
                synth.setSystemParameter('gain', 1.75);
                synth.setSystemParameter('effectsEnabled', true);
                synth.setSystemParameter('reverbGain', 1);
                synth.setSystemParameter('chorusGain', 1);
                synth.connect(destination);
                const cancel = reset => { if (!disposed) node.port.postMessage({lunaMusic: 1, reset}); };
                cancel(true);
                return {
                    reset() { cancel(true); },
                    stopMIDI() { cancel(false); },
                    send(message, atSeconds) {
                        if (disposed) return;
                        if (!message || message.length < 2 || message.length > 3 || message[0] < 128 || message[0] >= 240
                                || !Number.isFinite(atSeconds) || atSeconds > audio.currentTime + .5)
                            throw Error('Invalid or excessively delayed browser MIDI message');
                        synth.sendMessage(message, 0, {time: Math.max(audio.currentTime, atSeconds)});
                    },
                    dispose,
                    get active() { return !disposed; },
                    get voiceCount() { return disposed ? 0 : synth.voiceCount; }
                };
            };
            return new Promise((resolve, reject) => {
                const timer = setTimeout(() => { dispose(); reject(Error('SoundFont music initialization timed out')); }, 15000);
                initialize().then(value => { clearTimeout(timer); resolve(value); }, error => {
                    clearTimeout(timer); dispose(); reject(error);
                });
            });
        }
    };
})();
