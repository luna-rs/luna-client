/* Website controls stay separate from shared game input, rendering, and connection configuration. */
(() => {
    'use strict';
    const game = document.getElementById('game');
    const stage = document.getElementById('game-stage');
    const help = document.getElementById('help');
    const diagnostics = document.getElementById('diagnostics');
    const statusLabel = document.getElementById('status');

    function revealSection(id) {
        if (id === 'help' || id === 'diagnostics') help.open = true;
        if (id === 'diagnostics') diagnostics.open = true;
        if (id === 'settings') document.getElementById('settings').open = true;
    }
    document.querySelectorAll('[data-open]').forEach(link => {
        link.addEventListener('click', () => revealSection(link.dataset.open));
    });
    window.addEventListener('hashchange', () => revealSection(location.hash.slice(1)));
    revealSection(location.hash.slice(1));
    document.getElementById('focus-game').addEventListener('click', () => {
        game.scrollIntoView({block: 'nearest'});
        game.focus({preventScroll: true});
    });

    const fullscreen = document.getElementById('fullscreen-game');
    fullscreen.disabled = !document.fullscreenEnabled || typeof stage.requestFullscreen !== 'function';
    if (fullscreen.disabled) fullscreen.title = 'Fullscreen is unavailable in this browser.';
    fullscreen.addEventListener('click', async () => {
        try {
            if (document.fullscreenElement) await document.exitFullscreen();
            else await stage.requestFullscreen();
            game.focus({preventScroll: true});
        } catch (error) {
            console.warn('Fullscreen was unavailable: ' + error.message);
            fullscreen.title = 'Fullscreen was unavailable. You can keep playing in this window.';
        }
    });
    document.addEventListener('fullscreenchange', () => {
        fullscreen.textContent = document.fullscreenElement ? 'Leave fullscreen' : 'Fullscreen game';
    });

    // Pre-existing controls keep optional performance tools inside collapsed diagnostics instead of above the game.
    document.querySelectorAll('[data-benchmark]').forEach(button => {
        button.addEventListener('click', () => {
            const performance = window.LunaPerformance;
            if (!performance) {
                document.getElementById('performance-status').textContent = 'Start the game before recording a capture.';
                return;
            }
            if (button.dataset.benchmark === 'start') performance.start(30, document.getElementById('performance-label').value);
            if (button.dataset.benchmark === 'stop') performance.stop();
            if (button.dataset.benchmark === 'export') performance.exportJSON();
        });
    });

    // Keep runtime detail in the log while presenting short status messages around the game.
    const friendlyStatuses = new Map([
        ['Client running', 'Ready — log in below'],
        ['Client code could not load; see startup log', 'Unable to prepare the game. Open Help for details.'],
        ['Client could not start; see startup log', 'Unable to start the game. Open Help for details.']
    ]);
    function updateStatus() {
        const original = statusLabel.textContent;
        const friendly = friendlyStatuses.get(original)
            || (/(?:Exception|Error):/.test(original) ? 'Unable to start the game. Open Help for details.' : null);
        if (friendly) statusLabel.textContent = friendly;
        if (original === 'Client running') {
            document.getElementById('performance-status').textContent = 'Ready to capture.';
        }
        const text = statusLabel.textContent;
        statusLabel.dataset.state = /unable|wrong|error|exception/i.test(text) ? 'problem'
            : /^ready/i.test(text) ? 'ready' : 'busy';
    }
    new MutationObserver(updateStatus).observe(statusLabel, {childList: true, characterData: true, subtree: true});
    updateStatus();

    // CPU profiling is opt-in via ?profile; its asynchronously added panel belongs in Help too.
    const main = document.querySelector('main');
    const tools = document.getElementById('diagnostics-tools');
    new MutationObserver(() => {
        const profiling = document.getElementById('cpu-profiling');
        if (profiling && profiling.parentElement !== tools) tools.append(profiling);
    }).observe(main, {childList: true});
})();
