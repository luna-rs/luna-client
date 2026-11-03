import http from 'node:http';
import net from 'node:net';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { WebSocket, WebSocketServer } from 'ws';

const MAX_BYTES = 8 * 1024 * 1024;

function positiveInteger(value, name) {
    if (!Number.isInteger(value) || value < 1) throw new Error(`${name} must be a positive integer`);
}

/**
 * Creates a beta-only binary WebSocket bridge for a separately hosted static client.
 *
 * @author lare96
 */
export function createGateway({ targetHost = '85.155.184.204', targetPort = 43594,
    allowedOrigin = 'https://luna-rs.github.io', maxPayloadBytes = MAX_BYTES, maxQueuedBytes = MAX_BYTES,
    connectTimeoutMs = 10000, idleTimeoutMs = 180000, closeTimeoutMs = 2000 } = {}) {
    if (typeof targetHost !== 'string' || !targetHost
            || !(net.isIP(targetHost) || /^[a-zA-Z0-9][a-zA-Z0-9.-]*$/.test(targetHost))) {
        throw new Error('A fixed beta target host is required');
    }
    positiveInteger(targetPort, 'targetPort');
    if (targetPort > 65535) throw new Error('targetPort must be a valid TCP port');
    const origin = new URL(allowedOrigin);
    if (origin.protocol !== 'https:' || origin.origin !== allowedOrigin) {
        throw new Error('allowedOrigin must be one exact HTTPS origin without a path');
    }
    for (const [name, value] of Object.entries({ maxPayloadBytes, maxQueuedBytes, connectTimeoutMs,
        idleTimeoutMs, closeTimeoutMs })) positiveInteger(value, name);
    if (maxPayloadBytes > MAX_BYTES || maxQueuedBytes > MAX_BYTES) {
        throw new Error('Payload and queue limits cannot exceed 8 MiB');
    }

    const server = http.createServer((request, response) => {
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
        response.writeHead(url.pathname === '/game' ? 426 : 404).end();
    });
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
        if (url.pathname !== '/game' || url.searchParams.get('server') !== 'beta'
                || [...url.searchParams.keys()].some(key => key !== 'server')
                || url.searchParams.getAll('server').length !== 1
                || request.headers.origin !== allowedOrigin) {
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
            gameSocket.on('timeout', () => close(1011, 'Beta connection timed out'));
            gameSocket.on('error', () => close(1011, 'Beta server unavailable'));
            gameSocket.on('end', () => close(1000, 'Beta connection closed'));
            gameSocket.on('close', hadError => close(hadError ? 1011 : 1000, 'Beta connection closed'));
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
    const port = Number(process.env.LUNA_GATEWAY_PORT || 8810);
    positiveInteger(port, 'LUNA_GATEWAY_PORT');
    if (port > 65535) throw new Error('LUNA_GATEWAY_PORT must be a valid TCP port');
    const gateway = createGateway({ targetHost: process.env.LUNA_BETA_HOST || '85.155.184.204',
        targetPort: Number(process.env.LUNA_BETA_PORT || 43594),
        allowedOrigin: process.env.LUNA_ALLOWED_ORIGIN || 'https://luna-rs.github.io' });
    gateway.server.listen(port, '127.0.0.1', () => console.log(`Luna beta gateway listening on 127.0.0.1:${port}`));
    gateway.server.on('error', failure => {
        console.error(`Luna beta gateway failed: ${failure.code || 'listen error'}`);
        process.exitCode = 1;
    });
    for (const signal of ['SIGINT', 'SIGTERM']) process.once(signal, () => {
        gateway.close().then(() => { process.exitCode = 0; });
    });
}
