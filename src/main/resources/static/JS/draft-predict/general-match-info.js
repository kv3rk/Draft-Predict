document.addEventListener('DOMContentLoaded', init);

function init() {
    const patchSelect = document.getElementById('patchSelect');
    const seasonSelect = document.getElementById('seasonSelect');
    const draftTypeSelect = document.getElementById('draftTypeSelect');
    const firstPickSelect = document.getElementById('firstPickSelect');
    const applyBtn = document.getElementById('applyBtn');

    if (!patchSelect || !seasonSelect || !draftTypeSelect || !firstPickSelect || !applyBtn) {
        console.error('Required DOM elements not found');
        return;
    }

    applyBtn.addEventListener('click', handleApply);
}

async function handleApply() {
    const patch = document.getElementById('patchSelect').value;
    const season = document.getElementById('seasonSelect').value;
    const draftType = document.getElementById('draftTypeSelect').value;
    const firstPick = document.getElementById('firstPickSelect').value;
    const applyBtn = document.getElementById('applyBtn');

    if (!patch || !season || !draftType) {
        alert('Please select patch, season and draft type');
        return;
    }

    setLoadingState(applyBtn, true);

    try {
        const response = await fetch('/draft-predict/setup/match/info', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                patch: patch,
                season: season,
                draftType: draftType,
                firstPickSide: firstPick
            })
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const data = await response.json();
        document.dispatchEvent(new CustomEvent('draftSetupApplied', {
            detail: data
        }));
    } catch (err) {
        console.error('Apply error:', err);
        alert('Failed to apply match setup. Please try again.');
    } finally {
        setLoadingState(applyBtn, false);
    }
}

function setLoadingState(button, isLoading) {
    if (isLoading) {
        button.dataset.originalText = button.textContent;
        button.textContent = 'Applying...';
        button.disabled = true;
    } else {
        button.textContent = button.dataset.originalText || 'Apply';
        button.disabled = false;
    }
}