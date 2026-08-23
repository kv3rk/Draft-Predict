document.addEventListener('DOMContentLoaded', init);

let currentBanIndex = 0;
let currentBanOrder = [];
let isBanPhaseActive = false;

let currentPickIndex = 0;
let currentPickOrder = [];
let isPickPhaseActive = false;

let currentFirstPickSide = 'BLUE';
let currentDraftType = 'LEGACY';
let currentDraftConfig = null;
let isDraftFinished = false;

function init() {
    document.addEventListener('draftSetupApplied', handleDraftSetup);

    const select = document.getElementById('championSelect');
    if (select) {
        select.addEventListener('change', (e) => {
            const championName = e.target.value;
            if (!championName || isDraftFinished) {
                e.target.value = '';
                return;
            }

            if (typeof window.isChampionBanned === 'function' && window.isChampionBanned(championName)) {
                window.showToast('This champion is already banned');
                e.target.value = '';
                return;
            }

            if (typeof window.isChampionPicked === 'function' && window.isChampionPicked(championName)) {
                window.showToast('This champion is already picked and cannot be picked again');
                e.target.value = '';
                return;
            }

            if (isBanPhaseActive) {
                advanceBan(championName);
                e.target.value = '';
            } else if (isPickPhaseActive) {
                advancePick(championName);
                e.target.value = '';
            }
        });
    }

    // Recommendation click handlers (event delegation)
    const banRecContainer = document.getElementById('banRecommendations');
    if (banRecContainer) {
        banRecContainer.addEventListener('click', handleBanRecommendationClick);
    }

    const pickRecContainer = document.getElementById('pickRecommendations');
    if (pickRecContainer) {
        pickRecContainer.addEventListener('click', handlePickRecommendationClick);
    }

    // Champion grid click handler (clickable champion tiles)
    const championGrid = document.getElementById('championGrid');
    if (championGrid) {
        championGrid.addEventListener('click', handleChampionGridClick);
    }
}

// ================= CHAMPION GRID CLICK HANDLER =================
function handleChampionGridClick(e) {
    const item = e.target.closest('.champion-grid-item');
    if (!item) return;
    if (item.classList.contains('unavailable')) return;
    if (isDraftFinished) return;

    const championName = item.dataset.champion;
    if (!championName) return;

    if (typeof window.isChampionBanned === 'function' && window.isChampionBanned(championName)) {
        window.showToast('This champion is already banned');
        return;
    }

    if (typeof window.isChampionPicked === 'function' && window.isChampionPicked(championName)) {
        window.showToast('This champion is already picked');
        return;
    }

    if (isBanPhaseActive) {
        advanceBan(championName);
    } else if (isPickPhaseActive) {
        advancePick(championName);
    }
}

// Update champion grid tiles availability (mark banned/picked as unavailable)
function updateChampionGridAvailability() {
    const items = document.querySelectorAll('.champion-grid-item');
    items.forEach(item => {
        const championName = item.dataset.champion;
        if (!championName) return;
        const isBanned = typeof window.isChampionBanned === 'function' && window.isChampionBanned(championName);
        const isPicked = typeof window.isChampionPicked === 'function' && window.isChampionPicked(championName);
        item.classList.toggle('unavailable', isBanned || isPicked);
    });
}

// ================= RECOMMENDATION CLICK HANDLERS =================
function handleBanRecommendationClick(e) {
    const target = e.target.closest('.ban-rec-item');
    if (!target) return;
    if (!isBanPhaseActive || isDraftFinished) return;

    const championName = target.dataset.champion;
    if (championName) {
        advanceBan(championName);
    }
}

function handlePickRecommendationClick(e) {
    const target = e.target.closest('.pick-rec-item');
    if (!target) return;
    if (!isPickPhaseActive || isDraftFinished) return;

    const championName = target.dataset.champion;
    if (championName) {
        advancePick(championName);
    }
}

// ================= DRAFT CONFIG RESOLVER =================
// Resolve draft config by type from global window registry
function resolveDraftConfig(draftType) {
    switch (draftType) {
        case 'SOLOQ':
            return window.SoloQDraftConfig || null;
        case 'LEGACY':
            return window.LegacyDraftConfig || null;
        default:
            console.warn(`[Draft] Unknown draft type: ${draftType}. Falling back to LEGACY.`);
            return window.LegacyDraftConfig || null;
    }
}

// ================= DRAFT SETUP =================
function handleDraftSetup(event) {
    const detail = event.detail || {};
    currentFirstPickSide = detail.firstPickSide || 'BLUE';
    currentDraftType = detail.draftType || 'LEGACY';
    currentDraftConfig = resolveDraftConfig(currentDraftType);

    if (!currentDraftConfig) {
        console.error('[Draft] No config found for draft type:', currentDraftType);
        return;
    }

    currentBanIndex = 0;
    isBanPhaseActive = true;
    isPickPhaseActive = false;
    isDraftFinished = false;

    if (typeof window.resetBannedChampions === 'function') window.resetBannedChampions();
    if (typeof window.resetPickedChampions === 'function') window.resetPickedChampions();

    document.querySelectorAll('.ban-slot').forEach(slot => {
        slot.classList.remove('active-ban', 'ban-completed');
        slot.innerHTML = '';
    });

    document.querySelectorAll('.pick-slot').forEach(slot => {
        slot.classList.remove('active-pick', 'pick-completed', 'blue-pick', 'red-pick');
        const pickNum = slot.dataset.pick.split('-')[1];
        slot.innerHTML = `<span class="pick-number">${pickNum}</span>`;
    });

    // Reset champion grid availability on new draft
    document.querySelectorAll('.champion-grid-item').forEach(item => {
        item.classList.remove('unavailable');
    });

    const select = document.getElementById('championSelect');
    if (select) {
        select.disabled = false;
    }

    // Get first-phase orders from draft-specific config
    currentBanOrder = currentDraftConfig.getBanOrder(currentFirstPickSide);

    highlightCurrentBan();
    fetchBanRecommendations();
}

// ================= BAN PHASE LOGIC =================
function getCurrentBanSide() {
    const currentSlotId = currentBanOrder[currentBanIndex];
    return currentSlotId.startsWith('blue') ? 'blue' : 'red';
}

function highlightCurrentBan() {
    document.querySelectorAll('.ban-slot').forEach(slot => slot.classList.remove('active-ban'));

    if (currentBanIndex >= currentBanOrder.length || isDraftFinished) {
        isBanPhaseActive = false;
        return;
    }

    const currentSlotId = currentBanOrder[currentBanIndex];
    const slot = document.querySelector(`[data-ban="${currentSlotId}"]`);
    if (slot) slot.classList.add('active-ban');
}

async function fetchBanRecommendations() {
    if (!isBanPhaseActive || isDraftFinished) return;

    renderBanLoading();

    const bans = typeof window.getBannedChampionsBySide === 'function'
        ? window.getBannedChampionsBySide()
        : { blueSideBans: [], redSideBans: [] };

    const picks = typeof window.getPickedChampionsBySide === 'function'
        ? window.getPickedChampionsBySide()
        : { blueSidePicks: [], redSidePicks: [] };

    // Determine draft phase by slot number (1-3 = early, 4-5 = late)
    const currentSlotId = currentBanOrder[currentBanIndex];
    const slotNumber = parseInt(currentSlotId.split('-')[1], 10);
    const phaseEndpoint = slotNumber <= 3 ? 'early-phase-draft' : 'late-phase-draft';

    // Determine side of current ban
    const side = getCurrentBanSide();
    const endpoint = side === 'blue'
        ? `/draft-predict/blue-side-ban/recommendations/${phaseEndpoint}`
        : `/draft-predict/red-side-ban/recommendations/${phaseEndpoint}`;

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({
                blueSideBans: bans.blueSideBans,
                redSideBans: bans.redSideBans,
                blueSidePicks: picks.blueSidePicks,
                redSidePicks: picks.redSidePicks
            })
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        renderBanRecommendations(await response.json());
    } catch (err) {
        console.error('Failed to fetch ban recommendations:', err);
        renderBanRecommendations([]);
    }
}

function renderBanLoading() {
    const container = document.getElementById('banRecommendations');
    if (!container) return;
    container.innerHTML = '<div class="loading-spinner ban-spinner"></div>';
    container.style.display = 'flex';
}

function renderBanRecommendations(champions) {
    const container = document.getElementById('banRecommendations');
    if (!container) return;

    if (!champions || champions.length === 0 || isDraftFinished) {
        container.innerHTML = '';
        container.style.display = 'none';
        return;
    }

    container.innerHTML = champions
        .map(name => `<span class="ban-rec-item" data-champion="${name}">${name}</span>`)
        .join('');
    container.style.display = 'flex';
}

function advanceBan(championName) {
    if (!isBanPhaseActive || isDraftFinished) return;

    const currentSlotId = currentBanOrder[currentBanIndex];
    const side = getCurrentBanSide();

    const slot = document.querySelector(`[data-ban="${currentSlotId}"]`);
    if (slot) {
        slot.classList.remove('active-ban');
        slot.classList.add('ban-completed');
        slot.innerHTML = `<span class="ban-champion-name">${championName}</span>`;
    }

    if (typeof window.addBannedChampion === 'function') window.addBannedChampion(championName, side);

    // Update champion grid availability after ban
    updateChampionGridAvailability();

    currentBanIndex++;

    if (currentBanIndex < currentBanOrder.length) {
        highlightCurrentBan();
        fetchBanRecommendations();
    } else {
        isBanPhaseActive = false;
        document.querySelectorAll('.ban-slot').forEach(s => s.classList.remove('active-ban'));
        renderBanRecommendations([]);

        // Decide next phase based on draft config
        if (currentBanOrder.length === 6) {
            startPickPhase();
        } else {
            startSecondPickPhase();
        }
    }
}

function startSecondPickPhase() {
    console.log('[Draft] Starting second pick phase');
    currentPickIndex = 0;
    isPickPhaseActive = true;
    currentPickOrder = currentDraftConfig.getSecondPickOrder(currentFirstPickSide);
    highlightCurrentPick();
    fetchPickRecommendations();
}

// ================= PICK PHASE LOGIC =================
function startPickPhase() {
    currentPickIndex = 0;
    isPickPhaseActive = true;
    currentPickOrder = currentDraftConfig.getPickOrder(currentFirstPickSide);
    highlightCurrentPick();
    fetchPickRecommendations();
}

function startSecondBanPhase() {
    console.log('[Draft] Starting second ban phase');
    currentBanIndex = 0;
    isBanPhaseActive = true;
    isPickPhaseActive = false;
    currentBanOrder = currentDraftConfig.getSecondBanOrder(currentFirstPickSide);
    highlightCurrentBan();
    fetchBanRecommendations();
}

function getCurrentPickSide() {
    const currentSlotId = currentPickOrder[currentPickIndex];
    return currentSlotId.startsWith('blue') ? 'blue' : 'red';
}

function highlightCurrentPick() {
    document.querySelectorAll('.pick-slot').forEach(slot => {
        slot.classList.remove('active-pick', 'blue-pick', 'red-pick');
    });

    if (currentPickIndex >= currentPickOrder.length || isDraftFinished) {
        isPickPhaseActive = false;
        renderPickRecommendations([]);
        return;
    }

    const currentSlotId = currentPickOrder[currentPickIndex];
    const slot = document.querySelector(`[data-pick="${currentSlotId}"]`);
    if (slot) {
        slot.classList.add('active-pick');
        slot.classList.add(currentSlotId.startsWith('blue') ? 'blue-pick' : 'red-pick');
    }
}

async function fetchPickRecommendations() {
    if (!isPickPhaseActive || isDraftFinished) return;

    renderPickLoading();

    const side = getCurrentPickSide();

    const bans = typeof window.getBannedChampionsBySide === 'function' ? window.getBannedChampionsBySide() : { blueSideBans: [], redSideBans: [] };
    const picks = typeof window.getPickedChampionsBySide === 'function' ? window.getPickedChampionsBySide() : { blueSidePicks: [], redSidePicks: [] };

    // Determine draft phase by slot number (1-3 = early, 4-5 = late)
    const currentSlotId = currentPickOrder[currentPickIndex];
    const slotNumber = parseInt(currentSlotId.split('-')[1], 10);
    const phaseEndpoint = slotNumber <= 3 ? 'early-phase-draft' : 'late-phase-draft';

    const endpoint = side === 'blue'
        ? `/draft-predict/blue-side-pick/recommendations/${phaseEndpoint}`
        : `/draft-predict/red-side-pick/recommendations/${phaseEndpoint}`;

    try {
        const response = await fetch(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({
                blueSideBans: bans.blueSideBans,
                redSideBans: bans.redSideBans,
                blueSidePicks: picks.blueSidePicks,
                redSidePicks: picks.redSidePicks
            })
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        renderPickRecommendations(await response.json());
    } catch (err) {
        console.error('Failed to fetch pick recommendations:', err);
        renderPickRecommendations([]);
    }
}

function renderPickLoading() {
    const container = document.getElementById('pickRecommendations');
    if (!container) return;
    container.innerHTML = '<div class="loading-spinner pick-spinner"></div>';
    container.style.display = 'flex';
}

function renderPickRecommendations(champions) {
    const container = document.getElementById('pickRecommendations');
    if (!container) return;

    if (!champions || champions.length === 0 || isDraftFinished) {
        container.innerHTML = '';
        container.style.display = 'none';
        return;
    }

    container.innerHTML = champions
        .map(name => `<span class="pick-rec-item" data-champion="${name}">${name}</span>`)
        .join('');
    container.style.display = 'flex';
}

function advancePick(championName) {
    if (!isPickPhaseActive || isDraftFinished) return;

    const currentSlotId = currentPickOrder[currentPickIndex];
    const side = getCurrentPickSide();

    const slot = document.querySelector(`[data-pick="${currentSlotId}"]`);
    if (slot) {
        slot.classList.remove('active-pick', 'blue-pick', 'red-pick');
        slot.classList.add('pick-completed');
        const sideClass = side === 'blue' ? 'blue-picked' : 'red-picked';
        slot.innerHTML = `<span class="pick-champion-name picked ${sideClass}">${championName}</span>`;
    }

    if (typeof window.addPickedChampion === 'function') {
        window.addPickedChampion(championName, side);
    }

    // Update champion grid availability after pick
    updateChampionGridAvailability();

    currentPickIndex++;

    if (currentPickIndex < currentPickOrder.length) {
        highlightCurrentPick();
        fetchPickRecommendations();
    } else {
        isPickPhaseActive = false;
        document.querySelectorAll('.pick-slot').forEach(s => s.classList.remove('active-pick', 'blue-pick', 'red-pick'));
        renderPickRecommendations([]);

        if (currentPickOrder.length === 6) {
            startSecondBanPhase();
        } else {
            finishDraft();
        }
    }
}

function finishDraft() {
    isDraftFinished = true;
    isBanPhaseActive = false;
    isPickPhaseActive = false;

    renderBanRecommendations([]);
    renderPickRecommendations([]);

    document.querySelectorAll('.ban-slot').forEach(s => s.classList.remove('active-ban'));
    document.querySelectorAll('.pick-slot').forEach(s => s.classList.remove('active-pick', 'blue-pick', 'red-pick'));

    const select = document.getElementById('championSelect');
    if (select) {
        select.disabled = true;
    }
}

window.advanceBan = advanceBan;
window.advancePick = advancePick;