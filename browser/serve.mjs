import http from 'node:http';
import net from 'node:net';
import path from 'node:path';
import { createReadStream } from 'node:fs';
import { stat } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { WebSocketServer } from 'ws';

const directory = path.dirname(fileURLToPath(import.meta.url));
const defaultRoot = path.join(directory, 'build/site');
const defaultTargets = { development: { host: process.env.LUNA_DEV_HOST || '127.0.0.1', port: Number(process.env.LUNA_DEV_PORT || 43594) },
    beta: { host: process.env.LUNA_BETA_HOST || '85.155.184.204', port: Number(process.env.LUNA_BETA_PORT || 43594) } };
const types = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8',
    '.map': 'application/json', '.png': 'image/png', '.wasm': 'application/wasm' };

export function createPreviewServer({ root = defaultRoot, port = 8090, targets = defaultTargets,
    allowedOrigins = process.env.LUNA_ALLOWED_ORIGINS?.split(',').filter(Boolean) } = {}) {
    const origins = new Set(allowedOrigins || [`http://127.0.0.1:${port}`, `http://localhost:${port}`]);
    for (const target of Object.values(targets)) {
        if (!target.host || !Number.isInteger(target.port) || target.port < 1 || target.port > 65535) {
            throw new Error('Every configured game target needs a host and valid TCP port');
        }
    }
    const server = http.createServer(async (request, response) => {
        if (process.env.LUNA_PROFILE === '1') response.setHeader('Document-Policy', 'js-profiling');
        try {
            if (request.method !== 'GET' && request.method !== 'HEAD') {
                response.writeHead(405).end();
                return;
            }
            const url = new URL(request.url, 'http://localhost');
            if (url.pathname === '/client-config.json') {
                const body = JSON.stringify(targets);
                response.writeHead(200, {'Content-Type': 'application/json', 'Cache-Control': 'no-store'});
                response.end(request.method === 'HEAD' ? undefined : body);
                return;
            }
            const file = path.resolve(root, '.' + decodeURIComponent(url.pathname === '/' ? '/index.html' : url.pathname));
            if (!file.startsWith(root + path.sep)) {
                response.writeHead(403).end();
                return;
            }
            const metadata = await stat(file);
            if (!metadata.isFile()) {
                response.writeHead(404).end();
                return;
            }
            response.writeHead(200, {
                'Content-Type': types[path.extname(file)] || 'application/octet-stream',
                'Content-Length': metadata.size,
                'Cache-Control': 'no-cache'
            });
            if (request.method === 'HEAD') response.end();
            else createReadStream(file).on('error', () => response.destroy()).pipe(response);
        } catch (error) {
            response.writeHead(error.code === 'ENOENT' ? 404 : 500).end();
        }
    });

    const sockets = new WebSocketServer({ noServer: true, perMessageDeflate: false, maxPayload: 1024 * 1024 });
    server.on('upgrade', (request, socket, head) => {
        socket.on('error', () => socket.destroy());
        let url;
        try {
            url = new URL(request.url, 'http://localhost');
        } catch {
            socket.end('HTTP/1.1 400 Bad Request\r\nConnection: close\r\n\r\n');
            return;
        }
        const profile = url.searchParams.get('server');
        if (url.pathname !== '/game' || !Object.hasOwn(targets, profile) || !origins.has(request.headers.origin)) {
            socket.end('HTTP/1.1 403 Forbidden\r\nConnection: close\r\n\r\n');
            return;
        }
        sockets.handleUpgrade(request, socket, head, webSocket => {
            const gameSocket = net.createConnection(targets[profile]);
            const started = Date.now();
            let sentBytes = 0, receivedBytes = 0;
            console.log(`Game connection: ${profile} -> ${targets[profile].host}:${targets[profile].port}`);
            gameSocket.setNoDelay(true);
            gameSocket.setTimeout(15000, () => gameSocket.destroy(new Error('Game connection timed out')));
            gameSocket.on('connect', () => gameSocket.setTimeout(0));
            webSocket.on('message', (data, binary) => {
                if (!binary) {
                    webSocket.close(1003, 'Binary game packets required');
                    return;
                }
                sentBytes += data.length;
                if (!gameSocket.write(data)) webSocket.pause();
            });
            gameSocket.on('drain', () => webSocket.resume());
            gameSocket.on('data', data => {
                receivedBytes += data.length;
                gameSocket.pause();
                if (webSocket.readyState !== webSocket.OPEN) {
                    gameSocket.destroy();
                    return;
                }
                webSocket.send(data, { binary: true }, error => {
                    if (error) gameSocket.destroy();
                    else gameSocket.resume();
                });
            });
            gameSocket.on('error', error => {
                console.error(`${profile} game connection: ${error.code || error.message}`);
                webSocket.close(1011, 'Game server unavailable');
            });
            gameSocket.on('end', () => webSocket.close(1000, 'Game connection closed'));
            webSocket.on('close', () => {
                console.log(`Game closed: ${profile}, ${Date.now() - started} ms, sent ${sentBytes}, received ${receivedBytes} bytes`);
                gameSocket.destroy();
            });
            webSocket.on('error', () => gameSocket.destroy());
        });
    });

    return { server, webSockets: sockets };
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    const port = Number(process.argv[2] || 8090);
    const { server } = createPreviewServer({ port });
    const bind = process.env.LUNA_BIND || '127.0.0.1';
    server.listen(port, bind, () => console.log(`Luna TeaVM preview: http://${bind}:${port}/`));
}
