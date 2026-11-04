/**
 * Validates the browser descriptor generated from the selected Java properties profile.
 *
 * @author lare96
 */
import {createHash} from 'node:crypto';
import {lstat, readFile} from 'node:fs/promises';
import {isIP} from 'node:net';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const fields = ['clientName', 'host', 'port', 'websocketUrl', 'websiteUrl', 'webBindAddress',
    'webPort', 'webOrigin', 'websocketProxyPort', 'propertiesSha256'];

function invalid(key, requirement) {
    throw new Error(`Invalid browser setting '${key}': ${requirement}`);
}

function hostname(value, key) {
    if (typeof value !== 'string' || value !== value.trim() || !value || value.length > 253
            || !(isIP(value) || value.replace(/\.$/, '').split('.').every(label =>
                /^[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$/i.test(label)))) {
        invalid(key, 'requires an IP address or hostname');
    }
}

function port(value, key) {
    if (!Number.isInteger(value) || value < 1 || value > 65535) invalid(key, 'must be between 1 and 65535');
}

function url(value, key, protocols) {
    if (typeof value !== 'string' || value !== value.trim() || !value || /[\u0000-\u0020\u007f]/.test(value)) {
        invalid(key, 'requires an absolute URL');
    }
    let parsed;
    try { parsed = new URL(value); } catch { invalid(key, 'requires an absolute URL'); }
    if (!protocols.includes(parsed.protocol) || !parsed.hostname || parsed.username || parsed.password) {
        invalid(key, `requires ${protocols.join(' or ')} without embedded credentials`);
    }
    return parsed;
}

function loopback(value) {
    const host = value.toLowerCase().replace(/^\[|\]$/g, '').replace(/\.$/, '');
    return host === 'localhost' || host.endsWith('.localhost') || host === '::1' || host === '::'
        || host === '0.0.0.0' || /^127\./.test(host)
        || /^::ffff:(?:7f[0-9a-f]{2}:[0-9a-f]{1,4}|0:0)$/.test(host);
}

/**
 * Checks the generated settings without changing their values or introducing another profile.
 */
export function validateBrowserConfiguration(config) {
    if (!config || typeof config !== 'object' || Array.isArray(config)
            || Object.keys(config).length !== fields.length || fields.some(key => !Object.hasOwn(config, key))) {
        throw new Error('Browser configuration must contain exactly the generated client.properties settings');
    }
    if (typeof config.clientName !== 'string' || !config.clientName.trim() || config.clientName.length > 256
            || /[\u0000-\u001f\u007f]/.test(config.clientName)) invalid('clientName', 'requires a short display name');
    hostname(config.host, 'connectHost');
    hostname(config.webBindAddress, 'webBindAddress');
    port(config.port, 'connectPort + portOffset');
    port(config.webPort, 'webPort');
    port(config.websocketProxyPort, 'websocketProxyPort');
    if (config.websocketUrl !== 'same-origin') {
        const endpoint = url(config.websocketUrl, 'websocketUrl', ['ws:', 'wss:']);
        if (endpoint.hash) invalid('websocketUrl', 'must not contain a fragment');
    }
    url(config.websiteUrl, 'websiteUrl', ['http:', 'https:']);
    const origin = url(config.webOrigin, 'webOrigin', ['http:', 'https:']);
    if (origin.origin !== config.webOrigin || (origin.protocol === 'http:' && !loopback(origin.hostname))) {
        invalid('webOrigin', 'requires an exact HTTPS origin, or an HTTP loopback origin, without a path or trailing slash');
    }
    if (typeof config.propertiesSha256 !== 'string' || !/^[a-f0-9]{64}$/.test(config.propertiesSha256)) {
        invalid('propertiesSha256', 'requires the generated properties checksum; rebuild the browser site');
    }
    return config;
}

/**
 * Reads only the generated public descriptor and verifies its original properties snapshot.
 */
export async function loadBrowserConfiguration(directory) {
    const root = path.resolve(directory);
    for (const name of ['client-config.json', 'client.properties']) {
        const file = await lstat(path.join(root, name));
        if (!file.isFile() || file.isSymbolicLink()) throw new Error(`Browser build requires a regular ${name}`);
    }
    const [contents, properties] = await Promise.all([
        readFile(path.join(root, 'client-config.json'), 'utf8'), readFile(path.join(root, 'client.properties'))
    ]);
    const config = validateBrowserConfiguration(JSON.parse(contents));
    if (createHash('sha256').update(properties).digest('hex') !== config.propertiesSha256) {
        throw new Error('The browser configuration and client.properties differ; rebuild the browser site from the selected profile');
    }
    return config;
}

/**
 * Requires a remote secure game endpoint because GitHub Pages cannot proxy same-origin game traffic.
 */
export function validatePagesConfiguration(config) {
    validateBrowserConfiguration(config);
    if (config.websocketUrl === 'same-origin') {
        invalid('websocketUrl', 'GitHub Pages requires an external wss:// endpoint; it cannot proxy /game');
    }
    const endpoint = new URL(config.websocketUrl);
    if (endpoint.protocol !== 'wss:' || loopback(endpoint.hostname)) {
        invalid('websocketUrl', 'GitHub Pages requires a remotely reachable wss:// endpoint');
    }
    return config;
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    try {
        if (process.argv.length !== 4 || process.argv[2] !== '--pages') {
            throw new Error('Usage: node browser/configuration.mjs --pages <built-site-directory>');
        }
        const config = validatePagesConfiguration(await loadBrowserConfiguration(process.argv[3]));
        console.log(JSON.stringify({host: config.host, port: config.port, websocketUrl: config.websocketUrl}));
    } catch (failure) {
        console.error(failure.message);
        process.exitCode = 1;
    }
}
