/* Both browser backends compile the same Java client. No Java entry point runs until a backend is ready. */
(() => {
    'use strict';
    const requested = new URLSearchParams(location.search).get('backend') || 'auto';
    const status = document.getElementById('status');

    function script(source) {
        return new Promise((resolve, reject) => {
            const element = document.createElement('script');
            element.src = source;
            element.onload = resolve;
            element.onerror = () => reject(new Error('Client resource could not load: ' + source));
            document.head.append(element);
        });
    }

    async function javaScript() {
        await script('js/client.js');
        if (typeof window.main !== 'function') throw new Error('The client entry point is missing');
        window.LUNA_BROWSER_BACKEND = 'js';
    }

    async function webAssembly() {
        if (typeof WebAssembly !== 'object') throw new Error('WebAssembly is unavailable');
        await script('wasm/client.wasm-runtime.js');
        const instance = await window.TeaVM.wasmGC.load('wasm/client.wasm');
        if (typeof instance.exports.main !== 'function') throw new Error('The client entry point is missing');
        window.main = args => instance.exports.main(args);
        window.LUNA_BROWSER_BACKEND = 'wasm';
    }

    async function initialize() {
        try {
            if (!['auto', 'js', 'wasm'].includes(requested)) throw new Error('Unknown browser backend');
            if (requested === 'js') {
                await javaScript();
            } else {
                try {
                    await webAssembly();
                } catch (error) {
                    if (requested === 'wasm') throw error;
                    console.info('Loading the compatible browser client: ' + error.message);
                    await javaScript();
                }
            }
        } catch (error) {
            status.textContent = 'Client code could not load; see startup log';
            console.error(error);
            return;
        }
        try {
            await window.codeLoaded();
        } catch (error) {
            status.textContent = 'Client could not start; see startup log';
            console.error(error);
        }
    }

    initialize();
})();
