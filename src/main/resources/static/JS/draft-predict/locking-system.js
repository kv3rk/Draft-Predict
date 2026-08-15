document.addEventListener('DOMContentLoaded', init);

function init() {
    const lockOverlay = document.getElementById('lockOverlay');

    if (!lockOverlay) {
        console.error('Lock overlay element not found');
        return;
    }

    document.addEventListener('draftSetupApplied', handleUnlock);
}

function handleUnlock(event) {
    const lockOverlay = document.getElementById('lockOverlay');

    if (!lockOverlay) return;

    lockOverlay.classList.add('hidden');

    // Update center info with setup data
    const detail = event.detail || {};
    const patchEl = document.getElementById('draftPatch');
    const firstPickEl = document.getElementById('draftFirstPick');

    if (patchEl && detail.patch) {
        patchEl.textContent = 'Patch ' + detail.patch;
    }

    if (firstPickEl && detail.firstPickSide) {
        firstPickEl.textContent = 'First Pick: ' + detail.firstPickSide;
    }

    console.log('Draft phase unlocked:', detail);
}