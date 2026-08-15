document.addEventListener('DOMContentLoaded', init);

function init() {
    const lockOverlay = document.getElementById('lockOverlay');

    if (!lockOverlay) {
        console.error('Lock overlay element not found');
        return;
    }

    // Listen for setup applied event from general-match-info.js
    document.addEventListener('draftSetupApplied', handleUnlock);

    // Optional: lock on page load (already locked by default via CSS)
}

function handleUnlock(event) {
    const lockOverlay = document.getElementById('lockOverlay');

    if (!lockOverlay) return;

    lockOverlay.classList.add('hidden');

    console.log('Draft phase unlocked:', event.detail);
}