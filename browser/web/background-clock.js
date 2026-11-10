/**
 * Supplies bounded background update opportunities without accumulating work while the page is busy.
 *
 * @author lare96
 */
(() => {
    'use strict';
    const PERIOD_MILLIS = 20;
    let generation = -1;
    let active = false;
    let awaitingAck = false;
    let timer = 0;

    function cancel() {
        if (timer) clearTimeout(timer);
        timer = 0;
        awaitingAck = false;
        active = false;
    }

    function schedule() {
        if (!active || awaitingAck || timer) return;
        const ticket = generation;
        timer = setTimeout(() => {
            if (!active || ticket !== generation) return;
            timer = 0;
            if (awaitingAck) return;
            awaitingAck = true;
            self.postMessage({type: 'tick', generation});
        }, PERIOD_MILLIS);
    }

    self.addEventListener('message', event => {
        const message = event.data;
        if (!message || !Number.isSafeInteger(message.generation) || message.generation < 0) return;
        const next = message.generation;
        if (message.type === 'start') {
            if (next <= generation) return;
            cancel();
            generation = next;
            active = true;
            schedule();
        } else if (message.type === 'stop') {
            if (next < generation) return;
            cancel();
            generation = next;
        } else if (message.type === 'ack') {
            if (!active || next !== generation || !awaitingAck) return;
            awaitingAck = false;
            schedule();
        }
    });
})();
