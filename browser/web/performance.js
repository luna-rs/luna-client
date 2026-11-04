/* Optional benchmark controls; collection is disabled until explicitly started. */
window.LunaPerformanceInstall = (control, presentation) => {
    let inputTimes, inputLatencies;
    let inputQueued = 0, inputConsumed = 0, inputSamples = 0, inputDropped = 0;
    let heapAtStart, heapAtEnd;

    function heapSnapshot() {
        const memory = typeof performance !== 'undefined' ? performance.memory : undefined;
        return memory ? {usedJSHeapSize: memory.usedJSHeapSize, totalJSHeapSize: memory.totalJSHeapSize,
            jsHeapSizeLimit: memory.jsHeapSizeLimit} : null;
    }

    function withInputMetrics(result) {
        if (!result.schema) return result;
        result.browserHeap = {start: heapAtStart, end: heapAtEnd,
            limitation: 'Non-standard Chromium performance.memory; approximate JavaScript heap, excludes GPU/native'};
        const samples = inputLatencies ? Array.from(inputLatencies.subarray(0, inputSamples)) : [];
        const sorted = samples.slice().sort((left, right) => left - right);
        const percentile = fraction => sorted.length
            ? sorted[Math.max(0, Math.ceil(sorted.length * fraction) - 1)] : 0;
        result.inputEventToCanvas = {
            definition: 'DOM event timestamp to next completed canvas upload; mouse-down, key-down, and wheel',
            samples: samples.length,
            meanMs: samples.length ? samples.reduce((sum, value) => sum + value, 0) / samples.length : 0,
            medianMs: percentile(.5), p95Ms: percentile(.95), p99Ms: percentile(.99), maxMs: percentile(1),
            unpresentedEvents: inputQueued - inputConsumed,
            droppedEvents: inputDropped,
            rawMs: samples
        };
        return result;
    }

    const api = window.LunaPerformance = {
        enabled: false,
        start(seconds = 30, label = 'manual') {
            document.getElementById('game').focus({preventScroll: true});
            inputTimes = new Float64Array(32768);
            inputLatencies = new Float64Array(32768);
            inputQueued = inputConsumed = inputSamples = inputDropped = 0;
            api.enabled = true;
            control('start', seconds, label);
            heapAtStart = heapSnapshot();
            heapAtEnd = undefined;
            status.textContent = 'Capturing ' + seconds + ' seconds';
        },
        stop() {
            api.enabled = false;
            api.completed(JSON.parse(control('stop', 0, '')));
        },
        exportJSON() {
            if (api.enabled) api.stop();
            const result = withInputMetrics(JSON.parse(control('report', 0, '')));
            window.LUNA_PERFORMANCE_RESULT = result;
            const blob = new Blob([JSON.stringify(result, null, 2)], {type: 'application/json'});
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = 'luna-performance-' + (result.label || 'capture') + '.json';
            link.click();
            setTimeout(() => URL.revokeObjectURL(url), 1000);
            return result;
        },
        present(start, conversionEnd, uploadEnd) {
            if (!api.enabled) return;
            while (inputConsumed < inputQueued) {
                const latency = uploadEnd - inputTimes[inputConsumed++];
                if (latency >= 0 && latency < 120000 && inputSamples < inputLatencies.length) {
                    inputLatencies[inputSamples++] = latency;
                } else inputDropped++;
            }
            presentation(conversionEnd - start, uploadEnd - conversionEnd);
        },
        input(timestamp, type) {
            if (!api.enabled || type !== 1 && type !== 4 && type !== 9) return;
            if (!Number.isFinite(timestamp) || timestamp <= 0 || inputQueued >= inputTimes.length) {
                inputDropped++;
                return;
            }
            inputTimes[inputQueued++] = timestamp;
        },
        completed(result) {
            api.enabled = false;
            if (heapAtEnd === undefined) heapAtEnd = heapSnapshot();
            result = withInputMetrics(result);
            window.LUNA_PERFORMANCE_RESULT = result;
            status.textContent = 'Capture complete: ' + (result.presentedFps || 0).toFixed(1)
                + ' presented FPS';
            console.log('Luna performance: ' + JSON.stringify({label: String(result.label || '').slice(0, 128),
                durationSeconds: result.durationSeconds, renderedFps: result.renderedFps,
                presentedFps: result.presentedFps, renderMedianMs: result.renderProcessing?.medianMs,
                renderP95Ms: result.renderProcessing?.p95Ms, renderP99Ms: result.renderProcessing?.p99Ms,
                presentationP95Ms: result.presentationIntervals?.p95Ms,
                minimumTargetFps: result.minimumObservedTargetFps, reducedRateLoops: result.reducedRateLoops}));
            if (typeof location !== 'undefined' && new URLSearchParams(location.search).has('profile')
                    && typeof fetch === 'function') {
                fetch('/__luna_benchmark', {method: 'POST', headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({...result, kind: 'frames'})}).catch(() => {});
            }
        }
    };
    let panel = document.getElementById('performance-controls');
    if (!panel) {
        panel = document.createElement('div');
        panel.id = 'performance-controls';
        panel.style.cssText = 'margin:8px 0;display:flex;gap:8px;align-items:center';
        const label = document.createElement('input');
        label.id = 'performance-label';
        label.setAttribute('aria-label', 'Benchmark label');
        label.value = 'fixed-scene';
        const start = document.createElement('button');
        start.textContent = 'Capture 30 seconds';
        start.onclick = () => api.start(30, label.value);
        const stop = document.createElement('button');
        stop.textContent = 'Stop capture';
        stop.onclick = () => api.stop();
        const save = document.createElement('button');
        save.textContent = 'Export benchmark JSON';
        save.onclick = () => api.exportJSON();
        panel.append(label, start, stop, save);
        document.getElementById('game-container').after(panel);
    }
    let status = document.getElementById('performance-status');
    if (!status) {
        status = document.createElement('span');
        status.id = 'performance-status';
        status.textContent = 'Profiling disabled';
        panel.append(status);
    }
};
