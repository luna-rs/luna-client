import { readFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { preparePages } from './prepare-pages.mjs';

/**
 * Packages the webclient using the selected client.properties snapshot and generated release metadata.
 *
 * @author lare96
 */
export async function buildWebclient({ source, output } = {}) {
    const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
    const config = JSON.parse(await readFile(path.join(root, 'browser/pages-config.json'), 'utf8'));
    return preparePages({ siteDirectory: source || path.join(root, 'browser/build/site'),
        outputDirectory: output || path.join(root, 'build/webclient-site'), config });
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
    try {
        if (process.argv.length > 4) throw new Error('Usage: node browser/build-webclient.mjs [source-directory] [output-directory]');
        const result = await buildWebclient({ source: process.argv[2], output: process.argv[3] });
        console.log(JSON.stringify({ output: result.outputDirectory || path.resolve(process.argv[3] || 'build/webclient-site'),
            release: result.release, browserCommit: result.browserCommit, websocketUrl: result.websocketUrl,
            cacheFingerprint: result.cacheFingerprint }, null, 2));
    } catch (failure) {
        console.error(failure.message);
        process.exitCode = 1;
    }
}
