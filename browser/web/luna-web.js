/* Keep the historical game-page surround separate from client rendering and input. */
(() => {
    'use strict';
    const game = document.getElementById('game');
    const stage = document.getElementById('game-stage');
    const launch = document.getElementById('launch-screen');
    const statusLabel = document.getElementById('status');
    const help = document.getElementById('help');
    const diagnostics = document.getElementById('diagnostics');
    let started = false;

    function openDialog(id) {
        if (id === 'diagnostics') {
            diagnostics.open = true;
            id = 'help';
        }
        if (!['help', 'settings', 'security'].includes(id)) return;
        const dialog = document.getElementById(id);
        if (!dialog.open) dialog.showModal();
    }
    document.querySelectorAll('[data-dialog]').forEach(link => {
        link.addEventListener('click', event => {
            event.preventDefault();
            openDialog(link.dataset.dialog);
        });
    });
    window.addEventListener('hashchange', () => openDialog(location.hash.slice(1)));
    openDialog(location.hash.slice(1));
    document.querySelectorAll('dialog').forEach(dialog => {
        dialog.addEventListener('close', () => {
            if (started) game.focus({preventScroll: true});
        });
    });
    document.getElementById('start').addEventListener('click', () => { started = true; });
    document.getElementById('focus-game').addEventListener('click', () => {
        help.close();
        (started ? game : document.getElementById('start')).focus({preventScroll: true});
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
        fullscreen.textContent = document.fullscreenElement ? 'Leave fullscreen' : 'Fullscreen';
    });

    document.querySelectorAll('[data-benchmark]').forEach(button => {
        button.addEventListener('click', () => {
            const performance = window.LunaPerformance;
            if (!performance) return;
            if (button.dataset.benchmark === 'start') {
                help.close();
                performance.start(30, document.getElementById('performance-label').value);
            }
            if (button.dataset.benchmark === 'stop') performance.stop();
            if (button.dataset.benchmark === 'export') performance.exportJSON();
        });
    });

    const friendlyStatuses = new Map([
        ['Client running', 'Ready'],
        ['Client code could not load; see startup log', 'Unable to prepare the game. Open Manual for details.'],
        ['Client could not load; see startup log', 'Unable to load the game. Open Manual for details.'],
        ['Client could not start; see startup log', 'Unable to start the game. Open Manual for details.']
    ]);
    function updateStatus() {
        const original = statusLabel.textContent;
        const friendly = friendlyStatuses.get(original)
            || (/(?:Exception|Error):/.test(original) ? 'Unable to start the game. Open Manual for details.' : null);
        if (friendly) statusLabel.textContent = friendly;
        if (original === 'Client running') {
            started = true;
            launch.hidden = true;
            document.getElementById('performance-status').textContent = 'Ready to capture.';
        } else if (/unable|could not|wrong|error|exception/i.test(statusLabel.textContent)) {
            launch.hidden = false;
        }
    }
    new MutationObserver(updateStatus).observe(statusLabel, {childList: true, characterData: true, subtree: true});
    updateStatus();

    const main = document.querySelector('main');
    const tools = document.getElementById('diagnostics-tools');
    tools.addEventListener('click', event => {
        if (event.target.closest('button')) help.close();
    }, true);
    new MutationObserver(() => {
        const profiling = document.getElementById('cpu-profiling');
        if (profiling && profiling.parentElement !== tools) tools.append(profiling);
    }).observe(main, {childList: true});
})();
