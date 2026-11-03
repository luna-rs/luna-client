/* Optional local diagnostics. Loaded only when the page URL includes ?profile. */
(() => {
    'use strict';
    const parameters = new URLSearchParams(location.search);
    if (!parameters.has('profile')) return;

    function initialize() {
        const controls = document.createElement('fieldset');
        controls.id = 'cpu-profiling';
        const legend = document.createElement('legend');
        legend.textContent = 'Optional CPU diagnostics';
        const record = document.createElement('button');
        record.id = 'record-cpu';
        record.type = 'button';
        record.textContent = 'Record CPU profile (20 seconds)';
        const snapshot = document.createElement('button');
        snapshot.type = 'button';
        snapshot.textContent = 'Environment snapshot';
        const download = document.createElement('a');
        download.id = 'download-cpu-profile';
        download.textContent = 'Download CPU profile';
        download.hidden = true;
        download.style.marginLeft = '12px';
        const status = document.createElement('p');
        status.id = 'cpu-profile-status';
        status.setAttribute('role', 'status');
        const hint = document.createElement('p');
        hint.textContent = 'CPU profiling adds overhead. Run frame-time benchmarks separately.';
        controls.append(legend, record, snapshot, download, status, hint);
        (document.querySelector('main') || document.body).append(controls);

        let session;
        let traceUrl;

        function heap() {
            const memory = performance.memory;
            return memory ? {used: memory.usedJSHeapSize, total: memory.totalJSHeapSize,
                limit: memory.jsHeapSizeLimit} : null;
        }

        function environment() {
            const canvas = document.getElementById('game');
            const bounds = canvas?.getBoundingClientRect();
            return {userAgent: navigator.userAgent, platform: navigator.platform,
                logicalProcessors: navigator.hardwareConcurrency, deviceMemoryGiB: navigator.deviceMemory ?? null,
                pixelRatio: window.devicePixelRatio, crossOriginIsolated: window.crossOriginIsolated,
                secureContext: window.isSecureContext, documentHidden: document.hidden,
                canvas: canvas ? {width: canvas.width, height: canvas.height,
                    cssWidth: bounds.width, cssHeight: bounds.height} : null,
                heap: heap(), heapMetric: 'Non-standard performance.memory; browser estimates, not process RSS',
                cpuSamplingSupported: typeof window.Profiler === 'function'};
        }

        function sampleHeap(current) {
            const memory = heap();
            if (memory) current.heaps.push({elapsedMs: performance.now() - current.start, ...memory});
        }

        function collectTasks(current, entries) {
            for (const entry of entries) {
                if (entry.startTime < current.start || current.longTasks.length >= 4096) continue;
                current.longTasks.push({startMs: entry.startTime - current.start, durationMs: entry.duration});
            }
        }

        function hottestFrames(trace) {
            const counts = new Map();
            let stacks = 0;
            for (const sample of trace.samples) {
                if (sample.stackId === undefined) continue;
                const stack = trace.stacks[sample.stackId];
                if (!stack) continue;
                counts.set(stack.frameId, (counts.get(stack.frameId) || 0) + 1);
                stacks++;
            }
            return [...counts].sort((left, right) => right[1] - left[1]).slice(0, 25)
                .map(([id, samples]) => {
                    const frame = trace.frames[id];
                    return {name: frame.name, resource: trace.resources[frame.resourceId] ?? null,
                        line: frame.line ?? null, column: frame.column ?? null, samples,
                        percentOfStackSamples: stacks ? samples * 100 / stacks : 0};
                });
        }

        async function finish(reason) {
            const current = session;
            if (!current || current.finishing) return;
            current.finishing = true;
            clearTimeout(current.timeout);
            clearInterval(current.heapTimer);
            sampleHeap(current);
            if (current.observer) {
                collectTasks(current, current.observer.takeRecords());
                current.observer.disconnect();
            }
            status.textContent = 'Finishing CPU profile…';
            record.disabled = true;
            try {
                const durationMs = performance.now() - current.start;
                const trace = await current.profiler.stop();
                const result = {kind: 'cpu', label: 'cpu-' + Date.now(), schema: 1, reason, requestedIntervalMs: 1,
                    actualIntervalMs: current.profiler.sampleInterval, durationMs,
                    environment: current.environment, finalHeap: heap(), heapSamples: current.heaps,
                    longTaskSupport: current.longTaskSupport, longTasks: current.longTasks,
                    longTaskTotalMs: current.longTasks.reduce((sum, entry) => sum + entry.durationMs, 0),
                    hottestLeafFrames: hottestFrames(trace), trace};
                window.LUNA_CPU_PROFILE = result;
                const summary = {kind: result.kind, label: result.label, reason, durationMs,
                    requestedIntervalMs: result.requestedIntervalMs, actualIntervalMs: result.actualIntervalMs,
                    sampleCount: trace.samples.length, frameCount: trace.frames.length, stackCount: trace.stacks.length,
                    environment: result.environment, heapStart: current.heaps[0] ?? null,
                    heapEnd: current.heaps.at(-1) ?? null, longTaskCount: current.longTasks.length,
                    longTaskTotalMs: result.longTaskTotalMs, hottestLeafFrames: result.hottestLeafFrames};
                console.log('Luna CPU profile: ' + JSON.stringify(summary));
                const serialized = JSON.stringify(result);
                if (traceUrl) URL.revokeObjectURL(traceUrl);
                traceUrl = URL.createObjectURL(new Blob([serialized], {type: 'application/json'}));
                download.href = traceUrl;
                download.download = result.label + '.json';
                download.hidden = false;
                if (['localhost', '127.0.0.1', '[::1]', '::1'].includes(location.hostname)
                        && typeof fetch === 'function') {
                    fetch('/__luna_benchmark', {method: 'POST', credentials: 'omit',
                        headers: {'Content-Type': 'application/json'}, body: serialized}).catch(() => {});
                }
                status.textContent = 'Recorded ' + trace.samples.length + ' samples over '
                    + (durationMs / 1000).toFixed(1)
                    + ' seconds. Summary is in the console; full trace is downloadable.';
            } catch (error) {
                status.textContent = 'CPU profile failed: ' + error.message;
                console.warn('Luna CPU profile error: ' + error.message);
            } finally {
                session = undefined;
                record.disabled = false;
                record.textContent = 'Record CPU profile (20 seconds)';
            }
        }

        function start() {
            if (session) { finish('manual'); return; }
            try {
                const current = {start: performance.now(), heaps: [], longTasks: [],
                    environment: environment(), longTaskSupport: false};
                current.profiler = new window.Profiler({sampleInterval: 1, maxBufferSize: 30000});
                session = current;
                if (typeof PerformanceObserver === 'function'
                        && PerformanceObserver.supportedEntryTypes?.includes('longtask')) {
                    try {
                        current.observer = new PerformanceObserver(list => collectTasks(current, list.getEntries()));
                        current.observer.observe({type: 'longtask', buffered: false});
                        current.longTaskSupport = true;
                    } catch (error) {
                        current.observer?.disconnect();
                        current.observer = undefined;
                    }
                }
                sampleHeap(current);
                current.heapTimer = setInterval(() => sampleHeap(current), 1000);
                current.timeout = setTimeout(() => finish('duration'), 20000);
                current.profiler.addEventListener('samplebufferfull', () => finish('buffer-full'), {once: true});
                record.textContent = 'Stop CPU recording';
                status.textContent = 'Recording CPU samples for 20 seconds. Keep the game focused and visible.';
                document.getElementById('game')?.focus({preventScroll: true});
            } catch (error) {
                session = undefined;
                status.textContent = 'CPU sampling unavailable: ' + error.message
                    + '. The local server must enable the js-profiling Document-Policy.';
                console.warn('Luna CPU profile unavailable: ' + error.message);
            }
        }

        record.addEventListener('click', start);
        snapshot.addEventListener('click', () => {
            console.log('Luna environment: ' + JSON.stringify(environment()));
            status.textContent = 'Environment and available heap estimates were logged to the console.';
        });
        if (typeof window.Profiler !== 'function') {
            record.disabled = true;
            status.textContent = 'CPU sampling is unavailable in this browser. Environment snapshots remain available.';
        } else {
            status.textContent = 'CPU sampling is optional and inactive. No trace is uploaded.';
        }
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', initialize, {once: true});
    else initialize();
})();
