/* Content-addressed browser cache storage. Cache decoding remains in shared Java. */
(() => {
    'use strict';
    let database;
    function openDatabase() {
        if (!database) database = new Promise((resolve, reject) => {
            const request = indexedDB.open('luna-client-cache', 1);
            request.onupgradeneeded = () => request.result.createObjectStore('stores');
            request.onerror = () => reject(request.error);
            request.onsuccess = () => resolve(request.result);
            request.onblocked = () => reject(new Error('Browser cache database is blocked'));
        });
        return database;
    }
    function transaction(db, mode, action) {
        return new Promise((resolve, reject) => {
            const tx = db.transaction('stores', mode);
            const request = action(tx.objectStore('stores'));
            tx.oncomplete = () => resolve(request.result);
            tx.onerror = () => reject(tx.error || request.error);
            tx.onabort = () => reject(tx.error || new Error('Cache transaction aborted'));
        });
    }
    async function load(url, sha256) {
        async function verify(buffer) {
            if (!(buffer instanceof ArrayBuffer) || buffer.byteLength > 64 * 1024 * 1024)
                throw new Error('Invalid or oversized asset: ' + url);
            if (sha256 !== null) {
                if (!globalThis.crypto?.subtle) throw new Error('Cache checksums require HTTPS or localhost');
                const digest = await crypto.subtle.digest('SHA-256', buffer);
                const actual = Array.from(new Uint8Array(digest), value => value.toString(16).padStart(2, '0')).join('');
                if (actual !== sha256) throw new Error('Cache checksum mismatch: ' + url);
            }
            return buffer;
        }
        const key = sha256 + ':' + url.substring(url.lastIndexOf('/') + 1);
        let db;
        if (sha256 !== null) {
            try {
                db = await openDatabase();
                const cached = await transaction(db, 'readonly', store => store.get(key));
                if (cached) {
                    try {
                        await verify(cached);
                        console.log('Browser cache storage hit: ' + url);
                        return cached;
                    } catch (error) {
                        console.warn('Discarding invalid stored cache: ' + url);
                        await transaction(db, 'readwrite', store => store.delete(key));
                    }
                }
            } catch (error) { console.warn('Browser cache storage unavailable: ' + error); }
        }
        const downloadUrl = typeof window.LunaAssetUrl === 'function' ? window.LunaAssetUrl(url) : url;
        const response = await fetch(downloadUrl, {cache: 'no-cache'});
        if (!response.ok) throw new Error('Asset download failed: ' + url + ' (HTTP ' + response.status + ')');
        const buffer = await verify(await response.arrayBuffer());
        if (db) {
            try { await transaction(db, 'readwrite', store => store.put(buffer, key)); }
            catch (error) { console.warn('Browser cache was not persisted: ' + error); }
        }
        return buffer;
    }
    window.LunaAssetStore = {
        fetch(url, sha256, callback) {
            load(url, sha256).then(buffer => callback(new Int8Array(buffer), null))
                .catch(error => callback(null, String(error)));
        }
    };
})();
