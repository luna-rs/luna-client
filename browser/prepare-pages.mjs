import { createHash } from 'node:crypto';
import { lstat, mkdir, readFile, readdir, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { validateBrowserConfiguration } from './configuration.mjs';

const stores = ['main_file_cache.dat', ...Array.from({ length: 5 }, (_, index) => `main_file_cache.idx${index}`)];
const assets = ['index.html', 'cache.js', 'soundfont.js', 'audio-midi.js', 'platform.js', 'performance.js', 'profiling.js', 'client-loader.js',
    'luna-web.css', 'luna-web.js',
    ...['stoneback.gif', 'navbar.gif', 'navbar_mainmenu.gif', 'navbar_manual.gif', 'navbar_rules.gif']
        .map(name => `assets/theme/${name}`),
    ...['THIRD_PARTY_NOTICES.txt', 'teavm-LICENSE.txt', 'teavm-NOTICE.txt', 'ws-LICENSE.txt', 'spessasynth-NOTICE.txt']
        .map(name => `legal/${name}`),
    ...['spessasynth-controller.js', 'spessasynth-worklet.js', 'soundbank.sf2', 'soundbank.json']
        .map(name => `audio/${name}`),
    'js/client.js', 'wasm/client.wasm', 'wasm/client.wasm-runtime.js', 'client.properties', 'client-config.json',
    'cache/cache.sha256',
    ...stores.map(name => `cache/${name}`),
    ...['christmas', 'easter', 'halloween'].flatMap(season =>
        ['background.png', 'logo.png'].map(image => `assets/title/${season}/${image}`))];

function hash(bytes) {
    return createHash('sha256').update(bytes).digest('hex');
}

async function noLinks(target) {
    const absolute = path.resolve(target);
    let current = path.parse(absolute).root;
    for (const part of absolute.substring(current.length).split(path.sep).filter(Boolean)) {
        current = path.join(current, part);
        try {
            if ((await lstat(current)).isSymbolicLink()) throw new Error(`Symbolic links are not allowed: ${current}`);
        } catch (failure) {
            if (failure.code === 'ENOENT') return;
            throw failure;
        }
    }
}

function validateConfig(input) {
    if (!input || typeof input !== 'object' || Array.isArray(input)
            || Object.keys(input).some(key => !['release', 'sourceCommit'].includes(key))) {
        throw new Error('Release metadata must contain release and sourceCommit only');
    }
    if (typeof input.release !== 'string' || !/^[a-zA-Z0-9][a-zA-Z0-9._+-]{0,79}$/.test(input.release)) {
        throw new Error('Release must be a short version label');
    }
    if (typeof input.sourceCommit !== 'string' || !/^[a-f0-9]{40}$/i.test(input.sourceCommit)) {
        throw new Error('sourceCommit must be the full source Git commit');
    }
    return { release: input.release, sourceCommit: input.sourceCommit.toLowerCase() };
}

function htmlText(value) {
    return value.replace(/[&<>"']/g, character => ({'&': '&amp;', '<': '&lt;', '>': '&gt;',
        '"': '&quot;', "'": '&#39;'}[character]));
}

function configurationScript(browser, versions) {
    return 'window.LUNA_CLIENT_CONFIG = ' + JSON.stringify(browser) + ';\n'
        + 'window.LUNA_ASSET_VERSIONS = Object.freeze(' + JSON.stringify(versions) + ');\n'
        + 'window.LunaAssetUrl = function (name) {\n'
        + '    var base = new URL(".", document.baseURI);\n'
        + '    var url = new URL(name, document.baseURI);\n'
        + '    if (url.origin !== base.origin || !url.pathname.startsWith(base.pathname)) return name;\n'
        + '    var resource = url.pathname.slice(base.pathname.length);\n'
        + '    if (!Object.prototype.hasOwnProperty.call(window.LUNA_ASSET_VERSIONS, resource)) return name;\n'
        + '    var version = window.LUNA_ASSET_VERSIONS[resource];\n'
        + '    url.searchParams.set("v", version);\n'
        + '    return url.href;\n'
        + '};\n';
}

function versionHtmlAssets(html, versions) {
    return html.replace(/\b(src|href)=("|')([^"']+)\2/g, (attribute, name, quote, resource) => {
        return Object.hasOwn(versions, resource)
            ? name + '=' + quote + resource + '?v=' + versions[resource] + quote : attribute;
    });
}

/**
 * Copies a verified browser build and its selected properties profile into a separate website directory.
 *
 * @author lare96
 */
export async function preparePages({ siteDirectory, outputDirectory, config }) {
    const settings = validateConfig(config);
    const site = path.resolve(siteDirectory);
    const output = path.resolve(outputDirectory);
    const comparableSite = process.platform === 'win32' ? site.toLowerCase() : site;
    const comparableOutput = process.platform === 'win32' ? output.toLowerCase() : output;
    if (comparableSite === comparableOutput || comparableOutput.startsWith(comparableSite + path.sep)
            || comparableSite.startsWith(comparableOutput + path.sep)) {
        throw new Error('Input and output directories must be separate and must not contain each other');
    }
    await noLinks(site);
    await noLinks(output);
    if (!(await lstat(site)).isDirectory()) throw new Error('Input site must be a directory');
    try {
        if (!(await lstat(output)).isDirectory() || (await readdir(output)).length) {
            throw new Error('Output must be a new or empty directory');
        }
    } catch (failure) {
        if (failure.code !== 'ENOENT') throw failure;
    }
    const files = new Map();
    for (const name of assets) {
        const file = path.join(site, name);
        await noLinks(file);
        if (!(await lstat(file)).isFile()) throw new Error(`Missing regular build asset: ${name}`);
        const bytes = await readFile(file);
        if (!bytes.length) throw new Error(`Empty build asset: ${name}`);
        files.set(name, bytes);
    }
    if (files.get('cache/cache.sha256').some(byte => byte > 127)) throw new Error('Cache manifest must be ASCII');
    const expected = new Map();
    for (const line of files.get('cache/cache.sha256').toString('ascii').trimEnd().split(/\r?\n/)) {
        const match = /^([a-f0-9]{64})  (main_file_cache\.(?:dat|idx[0-4]))$/.exec(line);
        if (!match || expected.has(match[2])) throw new Error('Cache manifest must contain six unique store hashes');
        expected.set(match[2], match[1]);
    }
    if (expected.size !== stores.length) throw new Error('Cache manifest must describe all six stores');
    for (const name of stores) {
        if (hash(files.get(`cache/${name}`)) !== expected.get(name)) throw new Error(`Cache checksum mismatch: ${name}`);
    }
    const browser = validateBrowserConfiguration(JSON.parse(files.get('client-config.json').toString('utf8')));
    if (hash(files.get('client.properties')) !== browser.propertiesSha256) {
        throw new Error('The generated browser configuration and client.properties differ; rebuild the selected profile');
    }
    const originalHtml = files.get('index.html').toString('utf8');
    const serverSelector = /<select\b[^>]*\bid="server"[^>]*>[\s\S]*?<\/select>/g;
    if ((originalHtml.match(serverSelector) || []).length !== 1 || !originalHtml.includes('<script>')
            || originalHtml.split('<!-- LUNA_BUILD_LABEL -->').length !== 2) {
        throw new Error('Built page must contain one server selector, build label and its startup script');
    }
    const versions = Object.fromEntries([...files].filter(([name]) => name !== 'index.html')
        .map(([name, bytes]) => [name, hash(bytes)]));
    const boot = configurationScript(browser, versions);
    const html = versionHtmlAssets(originalHtml.replace(serverSelector,
        '<select id="server" aria-label="Server" disabled><option value="active" selected>'
            + htmlText(browser.clientName) + '</option></select>')
        .replace('<!-- LUNA_BUILD_LABEL -->', '<span id="release-version">' + settings.release + '</span>')
        .replace('<script>', '<script src="pages-config.js"></script>\n<script>'),
        {...versions, 'pages-config.js': hash(boot)});
    const fingerprint = hash(stores.map(name => `${expected.get(name)}  ${name}\n`).join(''));
    const provenance = { release: settings.release, sourceCommit: settings.sourceCommit,
        browserCommit: /^[a-f0-9]{40}$/i.test(process.env.GITHUB_SHA || '') ? process.env.GITHUB_SHA : null,
        clientName: browser.clientName, connection: {host: browser.host, port: browser.port},
        websocketUrl: browser.websocketUrl, cacheFingerprint: fingerprint,
        clientPropertiesSha256: hash(files.get('client.properties')),
        cacheStores: Object.fromEntries(expected),
        sourceAssets: Object.fromEntries([...files].map(([name, bytes]) => [name, hash(bytes)])) };
    await mkdir(output, { recursive: true });
    for (const name of assets) {
        const destination = path.join(output, name);
        await mkdir(path.dirname(destination), { recursive: true });
        await noLinks(destination);
        await writeFile(destination, name === 'index.html' ? html : files.get(name), { flag: 'wx' });
    }
    for (const [name, content] of Object.entries({
        'pages-config.js': boot,
        '.nojekyll': '', 'release-provenance.json': JSON.stringify(provenance, null, 2) + '\n'
    })) {
        await writeFile(path.join(output, name), content, { flag: 'wx' });
    }
    return { outputDirectory: output, ...provenance };
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    if (process.argv.length !== 5) {
        console.error('Usage: node browser/prepare-pages.mjs <built-site> <new-output> <config.json>');
        process.exitCode = 1;
    } else {
        try {
            const config = JSON.parse(await readFile(process.argv[4], 'utf8'));
            const result = await preparePages({ siteDirectory: process.argv[2],
                outputDirectory: process.argv[3], config });
            console.log(JSON.stringify(result, null, 2));
        } catch (failure) {
            console.error(failure.message);
            process.exitCode = 1;
        }
    }
}
