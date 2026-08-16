document.addEventListener('DOMContentLoaded', init);

const BAN_ORDER_BLUE_FIRST = [
    'blue-1', 'red-1', 'blue-2', 'red-2', 'blue-3', 'red-3'
];

const BAN_ORDER_RED_FIRST = [
    'red-1', 'blue-1', 'red-2', 'blue-2', 'red-3', 'blue-3'
];

let currentBanIndex = 0;
let currentBanOrder = [];
let isBanPhaseActive = false;

function init() {
    document.addEventListener('draftSetupApplied', handleDraftSetup);

    const select = document.getElementById('championSelect');
    if (select) {
        select.addEventListener('change', (e) => {
            const championName = e.target.value;
            if (!championName) return;

            if (isChampionBanned(championName)) {
                showToast('This champion is already banned');
                e.target.value = '';
                return;
            }

            if (isBanPhaseActive) {
                advanceBan(championName);
                e.target.value = '';
            }
        });
    }
}

function handleDraftSetup(event) {
    const detail = event.detail || {};
    const firstPick = detail.firstPickSide || 'BLUE';

    currentBanIndex = 0;
    isBanPhaseActive = true;

    resetBannedChampions();

    document.querySelectorAll('.ban-slot').forEach(slot => {
        slot.classList.remove('active-ban', 'ban-completed');
        slot.innerHTML = '';
    });

    if (firstPick === 'BLUE') {
        currentBanOrder = [...BAN_ORDER_BLUE_FIRST];
    } else {
        currentBanOrder = [...BAN_ORDER_RED_FIRST];
    }

    highlightCurrentBan();
    fetchBanRecommendations();
}

function getCurrentSide() {
    const currentSlotId = currentBanOrder[currentBanIndex];
    return currentSlotId.startsWith('blue') ? 'blue' : 'red';
}

function highlightCurrentBan() {
    document.querySelectorAll('.ban-slot').forEach(slot => {
        slot.classList.remove('active-ban');
    });

    if (currentBanIndex >= currentBanOrder.length) {
        isBanPhaseActive = false;
        return;
    }

    const currentSlotId = currentBanOrder[currentBanIndex];
    const slot = document.querySelector(`[data-ban="${currentSlotId}"]`);

    if (slot) {
        slot.classList.add('active-ban');
    }
}

async function fetchBanRecommendations() {
    if (!isBanPhaseActive) return;

    const { blueSideBans, redSideBans } = getBannedChampionsBySide();

    try {
        const response = await fetch('/draft-predict/ban/recommendations', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({
                blueSideBans: blueSideBans,
                redSideBans: redSideBans
            })
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        renderBanRecommendations(data);

    } catch (err) {
        console.error('Failed to fetch ban recommendations:', err);
        renderBanRecommendations([]);
    }
}

function renderBanRecommendations(champions) {
    const container = document.getElementById('banRecommendations');
    if (!container) return;

    if (!champions || champions.length === 0) {
        container.innerHTML = '';
        container.style.display = 'none';
        return;
    }

    container.innerHTML = champions
        .map(name => `<span class="ban-rec-item">${name}</span>`)
        .join('');
    container.style.display = 'flex';
}

function advanceBan(championName) {
    if (!isBanPhaseActive) return;

    const currentSlotId = currentBanOrder[currentBanIndex];
    const side = getCurrentSide();
    const slot = document.querySelector(`[data-ban="${currentSlotId}"]`);

    if (slot) {
        slot.classList.remove('active-ban');
        slot.classList.add('ban-completed');
        slot.innerHTML = `<span class="ban-champion-name">${championName}</span>`;
    }

    addBannedChampion(championName, side);

    currentBanIndex++;

    if (currentBanIndex < currentBanOrder.length) {
        highlightCurrentBan();
        fetchBanRecommendations();
    } else {
        isBanPhaseActive = false;
        document.querySelectorAll('.ban-slot').forEach(s => s.classList.remove('active-ban'));
        renderBanRecommendations([]);
    }
}

window.advanceBan = advanceBan;