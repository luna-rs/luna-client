import net from 'node:net';
import path from 'node:path';
import { createReadStream } from 'node:fs';
import { stat } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import { loadBrowserConfiguration, validateBrowserConfiguration } from './configuration.mjs';
import { createGateway, gatewayRoute } from './gateway.mjs';

const directory = path.dirname(fileURLToPath(import.meta.url));
const defaultRoot = path.join(directory, 'build/site');
const types = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8',
    '.css': 'text/css; charset=utf-8', '.json': 'application/json', '.map': 'application/json',
    '.png': 'image/png', '.gif': 'image/gif', '.jpg': 'image/jpeg', '.jpeg': 'image/jpeg',
    '.wasm': 'application/wasm', '.properties': 'text/plain; charset=utf-8', '.txt': 'text/plain; charset=utf-8' };

/**
 * Serves one built properties profile and optionally bridges its same-origin game connection.
 *
 * @author lare96
 */
export function createPreviewServer({ root = defaultRoot, config, port = config?.webPort, allowedOrigins } = {}) {
    validateBrowserConfiguration(config);
    if (!Number.isInteger(port) || port < 1 || port > 65535) throw new Error('The preview port must be between 1 and 65535');
    root = path.resolve(root);
    const body = JSON.stringify(config);
    const origins = allowedOrigins || [config.webOrigin, `http://127.0.0.1:${port}`,
        `http://localhost:${port}`, `http://[::1]:${port}`];
    const requestHandler = async (request, response) => {
        if (process.env.LUNA_PROFILE === '1') response.setHeader('Document-Policy', 'js-profiling');
        try {
            if (request.method !== 'GET' && request.method !== 'HEAD') {
                response.writeHead(405, {Allow: 'GET, HEAD'}).end();
                return;
            }
            const url = new URL(request.url, 'http://localhost');
            if (url.pathname === '/client-config.json') {
                response.writeHead(200, {'Content-Type': 'application/json', 'Cache-Control': 'no-store',
                    'Content-Length': Buffer.byteLength(body)});
                response.end(request.method === 'HEAD' ? undefined : body);
                return;
            }
            const file = path.resolve(root, '.' + decodeURIComponent(url.pathname === '/' ? '/index.html' : url.pathname));
            const relative = path.relative(root, file);
            if (relative === '..' || relative.startsWith('..' + path.sep) || path.isAbsolute(relative)) {
                response.writeHead(403).end();
                return;
            }
            const metadata = await stat(file);
            if (!metadata.isFile()) {
                response.writeHead(404).end();
                return;
            }
            response.writeHead(200, {
                'Content-Type': types[path.extname(file).toLowerCase()] || 'application/octet-stream',
                'Content-Length': metadata.size,
                'Cache-Control': 'no-cache'
            });
            if (request.method === 'HEAD') response.end();
            else createReadStream(file).on('error', () => response.destroy()).pipe(response);
        } catch (error) {
            if (!response.headersSent) response.writeHead(error.code === 'ENOENT' ? 404 : 500);
            response.end();
        }
    };
    return createGateway({ targetHost: config.host, targetPort: config.port, allowedOrigins: origins,
        ...gatewayRoute(config.websocketUrl), requestHandler, maxPayloadBytes: 1024 * 1024 });
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    try {
        if (process.argv.length > 3) throw new Error('Usage: node browser/serve.mjs [preview-port]');
        const config = await loadBrowserConfiguration(defaultRoot);
        const port = process.argv[2] === undefined ? config.webPort : Number(process.argv[2]);
        const preview = createPreviewServer({config, port});
        const bind = config.webBindAddress;
        const displayHost = net.isIP(bind) === 6 ? `[${bind}]` : bind;
        preview.server.listen(port, bind, () => console.log(`${config.clientName} browser preview: http://${displayHost}:${port}/`));
        preview.server.on('error', failure => {
            console.error(`Browser preview failed: ${failure.code || 'listen error'}`);
            process.exitCode = 1;
        });
        for (const signal of ['SIGINT', 'SIGTERM']) process.once(signal, () => {
            preview.close().then(() => { process.exitCode = 0; });
        });
    } catch (failure) {
        console.error(failure.message);
        process.exitCode = 1;
    }
}
