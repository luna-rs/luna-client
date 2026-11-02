package luna.browser.io;

import java.io.IOException;
import org.teavm.interop.Async;
import org.teavm.interop.AsyncCallback;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

/**
 * Bridges asynchronous browser asset and binary WebSocket operations to shared Java streams.
 *
 * @author lare96
 */
public final class BrowserIO {

    private BrowserIO() {
    }

    @Async
    public static native byte[] fetchBytes(String url) throws IOException;

    private static void fetchBytes(String url, AsyncCallback<byte[]> callback) {
        fetch(url, null, (data, error) -> completeBytes(data, error, callback));
    }

    @Async
    public static native byte[] fetchCheckedBytes(String url, String sha256) throws IOException;

    private static void fetchCheckedBytes(String url, String sha256, AsyncCallback<byte[]> callback) {
        fetch(url, sha256, (data, error) -> completeBytes(data, error, callback));
    }

    private static void completeBytes(JSObject data, String error, AsyncCallback<byte[]> callback) {
        if (error != null) {
            callback.error(new IOException(error));
            return;
        }
        if (data == null) {
            callback.complete(null);
            return;
        }
        int length = length(data);
        byte[] bytes = new byte[length];
        for (int index = 0; index < length; index++) bytes[index] = (byte) at(data, index);
        callback.complete(bytes);
    }

    @JSBody(params = {"url", "sha256", "callback"}, script = "window.LunaAssetStore.fetch(url, sha256, callback);")
    private static native void fetch(String url, String sha256, Completion callback);

    @JSBody(params = "data", script = "return data.length;")
    private static native int length(JSObject data);

    @JSBody(params = {"data", "index"}, script = "return data[index];")
    private static native int at(JSObject data, int index);

    @Async
    public static native JSObject openSocket(String host, int port) throws IOException;

    private static void openSocket(String host, int port, AsyncCallback<JSObject> callback) {
        connect(host, port, (state, error) -> {
            if (error != null) callback.error(new IOException(error));
            else callback.complete(state);
        });
    }

    @JSBody(params = {"host", "port", "callback"}, script = """
            const endpoint = window.LUNA_WEBSOCKET_URL;
            if (!endpoint) {
                callback(null, 'Browser networking gateway is not configured for ' + host + ':' + port);
                return;
            }
            try {
                const url = new URL(endpoint, location.href);
                const config = window.LUNA_CLIENT_CONFIG;
                const beta = config ? host === config.beta.host && port === config.beta.port : host === '85.155.184.204';
                if (config && !beta && (host !== config.development.host || port !== config.development.port)) {
                    callback(null, 'No browser gateway target configured for ' + host + ':' + port);
                    return;
                }
                url.searchParams.set('server', beta ? 'beta' : 'development');
                const socket = new WebSocket(url);
                socket.binaryType = 'arraybuffer';
                const state = {socket: socket, queue: [], offset: 0, closed: false, waiting: [], receivedBytes:0, sentBytes:0};
                window._lunaSocket = state;
                let completed = false;
                const finish = error => {
                    if (!completed) {
                        completed = true;
                        clearTimeout(timer);
                        callback(error ? null : state, error);
                    }
                };
                const notify = () => {
                    const waiting = state.waiting.splice(0);
                    waiting.forEach(waiter => waiter());
                };
                const timer = setTimeout(() => { finish('Browser gateway connection timed out'); socket.close(); }, 30000);
                socket.onopen = () => finish(null);
                socket.onmessage = event => {
                    state.receivedBytes += event.data.byteLength;
                    if (state.queue.reduce((count, data) => count + data.length, 0) - state.offset + event.data.byteLength > 8 * 1024 * 1024) {
                        state.closed = true; socket.close(1009, 'Game receive queue exceeded its limit'); notify(); return;
                    }
                    state.queue.push(new Int8Array(event.data)); notify();
                };
                socket.onerror = () => finish('Unable to connect to browser networking gateway');
                socket.onclose = () => { state.closed = true; finish('Browser networking gateway closed'); notify(); };
            } catch (error) {
                callback(null, String(error));
            }
            """)
    private static native void connect(String host, int port, Completion callback);

    @Async
    public static native byte[] readSocket(JSObject state, int length, int timeout) throws IOException;

    private static void readSocket(JSObject state, int length, int timeout, AsyncCallback<byte[]> callback) {
        receive(state, length, timeout, (data, error) -> completeBytes(data, error, callback));
    }

    @JSBody(params = {"state", "length", "timeout", "callback"}, script = """
            let finished = false;
            let timer = null;
            const finish = (data, error) => {
                if (finished) return;
                finished = true;
                if (timer !== null) clearTimeout(timer);
                const index = state.waiting.indexOf(poll);
                if (index >= 0) state.waiting.splice(index, 1);
                callback(data, error);
            };
            const poll = () => {
                while (state.queue.length && state.offset >= state.queue[0].length) {
                    state.queue.shift(); state.offset = 0;
                }
                if (state.queue.length) {
                    const first = state.queue[0];
                    const count = Math.min(length, first.length - state.offset);
                    const data = first.subarray(state.offset, state.offset + count);
                    state.offset += count;
                    finish(data, null);
                } else if (state.closed) finish(null, null);
                else state.waiting.push(poll);
            };
            if (timeout > 0) timer = setTimeout(() => finish(null, 'Browser socket read timed out'), timeout);
            poll();
            """)
    private static native void receive(JSObject state, int length, int timeout, Completion callback);

    @JSBody(params = "state", script = "return state.queue.reduce((count, data) => count + data.length, 0) - state.offset;")
    public static native int available(JSObject state);

    public static void writeSocket(JSObject state, byte[] bytes, int offset, int length) throws IOException {
        JSObject data = allocate(length);
        for (int index = 0; index < length; index++) put(data, index, bytes[offset + index]);
        String error = send(state, data);
        if (error != null) throw new IOException(error);
    }

    @JSBody(params = "length", script = "return new Int8Array(length);")
    private static native JSObject allocate(int length);

    @JSBody(params = {"data", "index", "value"}, script = "data[index] = value;")
    private static native void put(JSObject data, int index, int value);

    @JSBody(params = {"state", "data"}, script = """
            try {
                if (state.closed || state.socket.readyState !== WebSocket.OPEN) return 'Browser socket is closed';
                if (state.socket.bufferedAmount + data.length > 1024 * 1024) {
                    state.socket.close(1009, 'Game send queue exceeded its limit'); return 'Browser socket send queue is full';
                }
                state.socket.send(data);
                state.sentBytes += data.length;
                return null;
            } catch (error) { return String(error); }
            """)
    private static native String send(JSObject state, JSObject data);

    @JSBody(params = "state", script = "state.socket.close();")
    public static native void close(JSObject state);

    @JSBody(script = """
            try {
                let value = Number(localStorage.getItem('luna.teavm.uid'));
                if (!Number.isInteger(value) || value <= 0 || value > 99999999) {
                    value = 1 + Math.floor(Math.random() * 99999999);
                    localStorage.setItem('luna.teavm.uid', String(value));
                }
                return value;
            } catch (error) { return 1 + Math.floor(Math.random() * 99999999); }
            """)
    public static native int deviceId();

    /**
     * Receives browser data or a concrete browser failure.
     *
     * @author lare96
     */
    @JSFunctor
    private interface Completion extends JSObject {
        void complete(JSObject data, String error);
    }
}
