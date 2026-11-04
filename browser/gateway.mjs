import http from 'node:http';
import net from 'node:net';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { WebSocket, WebSocketServer } from 'ws';
import { loadBrowserConfiguration } from './configuration.mjs';

const MAX_BYTES = 8 * 1024 * 1024;

function positiveInteger(value, name) {
    if (!Number.isInteger(value) || value < 1) throw new Error(`${name} must be a positive integer`);
}

/**
 * Derives the bridge route from the selected properties profile, preserving its exact query.
 *
 * @author lare96
 */
export function gatewayRoute(websocketUrl) {
    const endpoint = new URL(websocketUrl === 'same-origin' ? 'ws://localhost/game' : websocketUrl);
    if (!['ws:', 'wss:'].includes(endpoint.protocol) || endpoint.username || endpoint.password || endpoint.hash) {
        throw new Error('The browser WebSocket URL must use ws: or wss: without credentials or a fragment');
    }
    return { gamePath: endpoint.pathname, gameSearch: endpoint.search };
}

function exactOrigin(value) {
    let origin;
    try { origin = new URL(value); } catch { throw new Error('Every allowed origin must be an exact HTTP or HTTPS origin'); }
    const host = origin.hostname.toLowerCase().replace(/^\[|\]$/g, '').replace(/\.$/, '');
    const local = host === 'localhost' || host.endsWith('.localhost') || host === '::1' || host === '::'
        || host === '0.0.0.0' || /^127\./.test(host)
        || /^::ffff:(?:7f[0-9a-f]{2}:[0-9a-f]{1,4}|0:0)$/.test(host);
    if (origin.origin !== value || (origin.protocol !== 'https:' && !(origin.protocol === 'http:' && local))) {
        throw new Error('Every allowed origin must use HTTPS, or HTTP on localhost, without a path');
    }
    return value;
}

/**
 * Creates a bounded binary WebSocket bridge to one explicitly configured game server.
 *
 * @author lare96
 */
export function createGateway({ targetHost, targetPort, allowedOrigin, allowedOrigins,
    gamePath = '/game', gameSearch = '', requestHandler,
    maxPayloadBytes = MAX_BYTES, maxQueuedBytes = MAX_BYTES,
    connectTimeoutMs = 10000, idleTimeoutMs = 180000, closeTimeoutMs = 2000 } = {}) {
    if (typeof targetHost !== 'string' || !targetHost
            || !(net.isIP(targetHost) || /^[a-zA-Z0-9][a-zA-Z0-9.-]*$/.test(targetHost))) {
        throw new Error('A fixed game target host is required');
    }
    positiveInteger(targetPort, 'targetPort');
    if (targetPort > 65535) throw new Error('targetPort must be a valid TCP port');
    const suppliedOrigins = allowedOrigins || [allowedOrigin];
    if (!Array.isArray(suppliedOrigins) || !suppliedOrigins.length) throw new Error('At least one exact browser origin is required');
    const origins = new Set(suppliedOrigins.map(exactOrigin));
    if (typeof gamePath !== 'string' || !gamePath.startsWith('/') || /[?#]/.test(gamePath)
            || typeof gameSearch !== 'string' || (gameSearch && !gameSearch.startsWith('?')) || gameSearch.includes('#')) {
        throw new Error('The game route must have an absolute path and an optional query');
    }
    if (requestHandler !== undefined && typeof requestHandler !== 'function') throw new Error('requestHandler must be a function');
    for (const [name, value] of Object.entries({ maxPayloadBytes, maxQueuedBytes, connectTimeoutMs,
        idleTimeoutMs, closeTimeoutMs })) positiveInteger(value, name);
    if (maxPayloadBytes > MAX_BYTES || maxQueuedBytes > MAX_BYTES) {
        throw new Error('Payload and queue limits cannot exceed 8 MiB');
    }

    const server = http.createServer(requestHandler || ((request, response) => {
        let url;
        try { url = new URL(request.url, 'http://localhost'); }
        catch {
            response.writeHead(400).end();
            return;
        }
        if (!['GET', 'HEAD'].includes(request.method)) {
            response.writeHead(405, { Allow: 'GET, HEAD' }).end();
            return;
        }
        if (url.pathname === '/health' || url.pathname === '/healthz') {
            const body = '{"status":"ok"}\n';
            response.writeHead(200, { 'Content-Type': 'application/json', 'Cache-Control': 'no-store',
                'Content-Length': Buffer.byteLength(body) });
            response.end(request.method === 'HEAD' ? undefined : body);
            return;
        }
        response.writeHead(url.pathname === gamePath ? 426 : 404).end();
    }));
    server.headersTimeout = 10000;
    server.requestTimeout = 15000;
    server.on('clientError', (_failure, socket) => socket.destroy());

    const webSockets = new WebSocketServer({ noServer: true, perMessageDeflate: false,
        maxPayload: maxPayloadBytes });
    server.on('upgrade', (request, socket, head) => {
        socket.on('error', () => socket.destroy());
        let url;
        try { url = new URL(request.url, 'http://localhost'); }
        catch {
            socket.end('HTTP/1.1 400 Bad Request\r\nConnection: close\r\n\r\n');
            return;
        }
        if (url.pathname !== gamePath || url.search !== gameSearch || !origins.has(request.headers.origin)) {
            socket.end('HTTP/1.1 403 Forbidden\r\nConnection: close\r\n\r\n');
            return;
        }
        webSockets.handleUpgrade(request, socket, head, webSocket => {
            const gameSocket = net.createConnection({ host: targetHost, port: targetPort });
            let closeTimer;
            let finished = false;
            const cleanup = () => {
                if (finished) return;
                finished = true;
                clearTimeout(closeTimer);
                gameSocket.destroy();
            };
            const close = (code, reason) => {
                gameSocket.destroy();
                if (webSocket.readyState === WebSocket.OPEN) {
                    webSocket.close(code, reason);
                    closeTimer = setTimeout(() => webSocket.terminate(), closeTimeoutMs);
                    closeTimer.unref();
                }
            };
            gameSocket.setNoDelay(true);
            gameSocket.setTimeout(connectTimeoutMs);
            gameSocket.on('connect', () => gameSocket.setTimeout(idleTimeoutMs));
            gameSocket.on('timeout', () => close(1011, 'Game connection timed out'));
            gameSocket.on('error', () => close(1011, 'Game server unavailable'));
            gameSocket.on('end', () => close(1000, 'Game connection closed'));
            gameSocket.on('close', hadError => close(hadError ? 1011 : 1000, 'Game connection closed'));
            gameSocket.on('drain', () => {
                if (webSocket.readyState === WebSocket.OPEN) webSocket.resume();
            });
            webSocket.on('message', (data, binary) => {
                if (!binary) {
                    close(1003, 'Binary game packets required');
                    return;
                }
                if (gameSocket.writableLength + data.length > maxQueuedBytes) {
                    close(1009, 'Game send queue exceeded its limit');
                    return;
                }
                if (!gameSocket.destroyed && !gameSocket.write(data)) webSocket.pause();
            });
            gameSocket.on('data', data => {
                gameSocket.pause();
                if (webSocket.readyState !== WebSocket.OPEN) {
                    gameSocket.destroy();
                    return;
                }
                if (webSocket.bufferedAmount + data.length > maxQueuedBytes) {
                    close(1009, 'Game receive queue exceeded its limit');
                    return;
                }
                webSocket.send(data, { binary: true }, failure => {
                    if (failure) close(1011, 'Browser connection closed');
                    else if (!gameSocket.destroyed) gameSocket.resume();
                });
            });
            webSocket.on('close', cleanup);
            webSocket.on('error', cleanup);
        });
    });

    let closing;
    const close = () => {
        if (closing) return closing;
        closing = new Promise(resolve => {
            for (const socket of webSockets.clients) socket.terminate();
            webSockets.close();
            server.close(resolve);
            server.closeIdleConnections();
        });
        return closing;
    };
    return { server, webSockets, close };
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    try {
        if (process.argv.length > 2) throw new Error('Configure the gateway listener in client.properties before building');
        const root = path.join(path.dirname(fileURLToPath(import.meta.url)), 'build/site');
        const configuration = await loadBrowserConfiguration(root);
        const gateway = createGateway({ targetHost: configuration.host, targetPort: configuration.port,
            allowedOrigin: configuration.webOrigin, ...gatewayRoute(configuration.websocketUrl) });
        const bind = configuration.webBindAddress;
        const port = configuration.websocketProxyPort;
        gateway.server.listen(port, bind, () => console.log(`${configuration.clientName} gateway listening on ${bind}:${port}`));
        gateway.server.on('error', failure => {
            console.error(`Browser gateway failed: ${failure.code || 'listen error'}`);
            process.exitCode = 1;
        });
        for (const signal of ['SIGINT', 'SIGTERM']) process.once(signal, () => {
            gateway.close().then(() => { process.exitCode = 0; });
        });
    } catch (failure) {
        console.error(failure.message);
        process.exitCode = 1;
    }
}
