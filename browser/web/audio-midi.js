/**
 * Sequences shared cache music through the existing browser audio context.
 *
 * @author lare96
 */
(() => {
    'use strict';
    const MAX_BYTES = 2 * 1024 * 1024, MAX_EVENTS = 100000, MAX_DURATION = 3600000;
    let synth, context, gain, fade, limiter, output, loading, pending, playing, volume = 0, closed = false;
    let synthGeneration = 0;

    function midi(data) {
        if (Array.isArray(data)) {
            if (!data.length || data.length > MAX_BYTES || data.some(value => !Number.isInteger(value) || value < -128 || value > 255))
                throw Error('Invalid or oversized MIDI byte array');
            data = Uint8Array.from(data);
        }
        let bytes = data instanceof ArrayBuffer ? new Uint8Array(data)
            : ArrayBuffer.isView(data) ? new Uint8Array(data.buffer, data.byteOffset, data.byteLength) : null;
        if (!bytes?.length || bytes.length > MAX_BYTES) throw Error('Missing or oversized MIDI data');
        const match = (position, text) => [...text].every((letter, index) => bytes[position + index] === letter.charCodeAt(0));
        const little = position => new DataView(bytes.buffer, bytes.byteOffset, bytes.byteLength).getUint32(position, true);
        if (match(0, 'RIFF')) {
            if (bytes.length < 12 || !match(8, 'RMID') || little(4) + 8 > bytes.length) throw Error('Invalid RIFF/RMID music');
            const end = little(4) + 8;
            let found;
            for (let position = 12; position + 8 <= end;) {
                const length = little(position + 4), next = position + 8 + length;
                if (next > end) throw Error('Truncated RIFF/RMID music');
                if (match(position, 'data')) { found = bytes.subarray(position + 8, next); break; }
                position = next + (length & 1);
            }
            if (!found) throw Error('RIFF/RMID music has no MIDI data');
            bytes = found;
        }
        const view = new DataView(bytes.buffer, bytes.byteOffset, bytes.byteLength);
        if (bytes.length < 14 || !match(0, 'MThd') || view.getUint32(4) !== 6) throw Error('Invalid MIDI header');
        const format = view.getUint16(8), tracks = view.getUint16(10), division = view.getUint16(12);
        if (format > 1 || tracks < 1 || tracks > 64 || format === 0 && tracks !== 1 || !division || division & 0x8000)
            throw Error('Unsupported MIDI format, track count, or SMPTE timing');
        const events = [];
        let position = 14, count = 0;
        for (let track = 0; track < tracks; track++) {
            if (position + 8 > bytes.length || !match(position, 'MTrk')) throw Error('Missing MIDI track');
            const end = position + 8 + view.getUint32(position + 4);
            position += 8;
            if (end > bytes.length) throw Error('Truncated MIDI track');
            let tick = 0, running = 0, ended = false;
            const next = () => {
                if (position >= end) throw Error('Truncated MIDI event');
                return bytes[position++];
            };
            const variable = () => {
                let value = 0;
                for (let index = 0; index < 4; index++) {
                    const byte = next(); value = value * 128 + (byte & 127);
                    if (!(byte & 128)) return value;
                }
                throw Error('Oversized MIDI delta or event length');
            };
            while (position < end) {
                if (++count > MAX_EVENTS) throw Error('MIDI contains too many events');
                tick += variable();
                if (tick > 0x7fffffff) throw Error('MIDI timeline is oversized');
                let status = next();
                if (status < 128) {
                    if (!running) throw Error('MIDI running status has no channel message');
                    position--; status = running;
                }
                if (status === 255) {
                    const type = next(), length = variable();
                    if (position + length > end) throw Error('Truncated MIDI metadata');
                    if (type === 81) {
                        if (length !== 3) throw Error('Invalid MIDI tempo');
                        const tempo = bytes[position] * 65536 + bytes[position + 1] * 256 + bytes[position + 2];
                        if (!tempo) throw Error('Invalid MIDI tempo');
                        events.push({tick, tempo});
                    }
                    position += length;
                    if (type === 47) {
                        if (length) throw Error('Invalid MIDI track end');
                        events.push({tick}); ended = true; position = end;
                    }
                } else if (status === 240 || status === 247) {
                    running = 0;
                    const length = variable();
                    if (position + length > end) throw Error('Truncated MIDI system message');
                    position += length;
                } else if (status >= 128 && status < 240) {
                    running = status;
                    const message = [status, next()];
                    if ((status & 240) !== 192 && (status & 240) !== 208) message.push(next());
                    if (message.slice(1).some(value => value > 127)) throw Error('Invalid MIDI channel data');
                    events.push({tick, message});
                } else throw Error('Unsupported MIDI event');
            }
            if (!ended) throw Error('MIDI track has no end marker');
        }
        events.sort((left, right) => left.tick - right.tick);
        let tempo = 500000, tick = 0, milliseconds = 0;
        const notes = [];
        for (const event of events) {
            milliseconds += (event.tick - tick) * tempo / division / 1000;
            if (!Number.isFinite(milliseconds) || milliseconds > MAX_DURATION) throw Error('MIDI duration exceeds one hour');
            tick = event.tick;
            if (event.tempo) tempo = event.tempo;
            else if (event.message) notes.push({time: milliseconds / 1000, message: event.message});
        }
        if (!notes.some(event => (event.message[0] & 240) === 144 && event.message[2] > 0)) throw Error('MIDI has no playable notes');
        return {events: notes, duration: Math.max(20, milliseconds)};
    }

    function report(request, duration) {
        const callback = request?.started;
        if (request) request.started = null;
        if (typeof callback === 'function') {
            try { callback(duration); }
            catch (error) { console.warn('Browser music callback: ' + error); }
        }
    }
    function applyVolume() {
        if (gain) gain.gain.value = Math.pow(10, (volume - 300) / 2000);
    }
    function stopPlaying() {
        const active = playing;
        playing = null;
        if (fade && context) {
            fade.gain.cancelScheduledValues(context.currentTime);
            fade.gain.setValueAtTime(0, context.currentTime);
        }
        try { synth?.stopMIDI(); } catch (error) { console.warn('Browser music stop: ' + error); }
        report(active, -1);
    }
    function disposeSynth() {
        synthGeneration++;
        loading = undefined;
        try { synth?.dispose(); } catch {}
        fade?.disconnect();
        gain?.disconnect();
        limiter?.disconnect();
        output?.disconnect();
        synth = context = gain = fade = limiter = output = undefined;
    }
    function stop() {
        const waiting = pending; pending = null;
        stopPlaying(); report(waiting, -1);
    }
    function pause() {
        if (!pending && playing?.loop) {
            pending = {song: playing.song, loop: true, started: playing.started};
            playing.started = null;
        }
        if (pending && !pending.loop) {
            const waiting = pending; pending = null; report(waiting, -1);
        }
        stopPlaying();
    }
    function ensureSynth() {
        const audio = window._lunaAudio;
        if (!audio || audio.state !== 'running' || closed) return false;
        if (context !== audio || synth?.active === false) {
            stopPlaying(); disposeSynth();
            context = audio; gain = audio.createGain();
            // Desktop Java Sound limits dense mixes before output; retain level without hard-clipping peaks.
            limiter = audio.createDynamicsCompressor();
            limiter.threshold.value = -.5; limiter.knee.value = 0; limiter.ratio.value = 20;
            limiter.attack.value = 0; limiter.release.value = .1;
            // Leave a small output margin for the browser compressor's finite attack and makeup gain.
            output = audio.createGain(); output.gain.value = .95;
            gain.connect(limiter); limiter.connect(output); output.connect(audio.destination); applyVolume();
            fade = audio.createGain(); fade.gain.value = 0; fade.connect(gain);
        }
        if (!synth && !loading) {
            const generation = synthGeneration, destination = fade;
            loading = Promise.resolve().then(() => generation === synthGeneration && !closed
                    ? window.LunaSoundFont.create(audio, destination) : null)
                .then(ready => {
                    if (!ready) return;
                    if (generation !== synthGeneration || closed || context !== audio || audio.state === 'closed') {
                        ready.dispose(); return;
                    }
                    synth = ready; loading = undefined; contextReady();
                }).catch(error => {
                    if (generation !== synthGeneration) return;
                    const waiting = pending;
                    const interrupted = waiting?.loop && audio.state !== 'running';
                    pending = interrupted ? waiting : null;
                    stopPlaying(); disposeSynth();
                    if (!interrupted) { report(waiting, -1); console.warn('Browser SoundFont: ' + error); }
                });
        }
        return !!synth;
    }
    function contextReady() {
        if (playing && (synth?.active === false || context !== window._lunaAudio || context.state === 'closed')) pause();
        if (!pending) return;
        const request = pending;
        try {
            if (!ensureSynth()) return;
            pending = null;
            synth.reset();
            const current = {...request, start: context.currentTime + .03};
            request.started = null;
            playing = current;
            fade.gain.cancelScheduledValues(context.currentTime);
            fade.gain.setValueAtTime(request.loop ? 0 : 1, current.start);
            if (request.loop) fade.gain.linearRampToValueAtTime(1, current.start + 1);
            synth.playTimeline(request.song, request.loop, current.start, event => {
                if (playing !== current) return;
                if (event.type === 'started') report(current, event.duration);
                else if (event.type === 'ended') stopPlaying();
                else if (event.type === 'error') {
                    stopPlaying();
                    console.warn('Browser music: ' + event.error);
                }
            });
        } catch (error) {
            pending = null; stopPlaying(); report(request, -1); console.warn('Browser music: ' + error);
        }
    }
    window.LunaMidi = {
        play(data, loop, started) {
            stop();
            const request = {started};
            try {
                if (closed) throw Error('Browser music is not active');
                request.song = midi(data); request.loop = !!loop; pending = request;
                contextReady();
            } catch (error) {
                pending = null; report(request, -1); console.warn('Browser music: ' + error);
            }
        },
        stop,
        prepareTransition() { const waiting = pending; pending = null; report(waiting, -1); },
        setVolume(value) {
            volume = Number.isFinite(value) ? Math.max(-9600, Math.min(0, value)) : -9600;
            if (volume <= -9600) stop();
            applyVolume();
        },
        close() { stop(); disposeSynth(); closed = true; },
        contextReady,
        status() { return {playing: !!playing, pending: !!pending, duration: playing?.song.duration || pending?.song.duration || 0,
            events: playing?.song.events.length || pending?.song.events.length || 0, voices: synth?.voiceCount || 0,
            engine: 'soundfont', loading: !!loading}; }
    };
    window.addEventListener('pagehide', pause);
    window.addEventListener('pageshow', contextReady);
})();
