document.addEventListener('DOMContentLoaded', init);

/* ─── Initialization ─── */
function init() {
    const championSelect = document.getElementById('championSelect');
    const searchBtn = document.getElementById('searchBtn');

    if (!championSelect || !searchBtn) {
        console.error('Required DOM elements not found');
        return;
    }

    loadChampionList();
    searchBtn.addEventListener('click', handleSearch);
}

/* ─── Load Champion List ─── */
async function loadChampionList() {
    const championSelect = document.getElementById('championSelect');

    try {
        const response = await fetch('/draft-predict/get/champion-list', {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}: ${response.statusText}`);
        }

        const champions = await response.json();

        if (!Array.isArray(champions) || champions.length === 0) {
            championSelect.innerHTML = '<option value="" disabled>No champions found</option>';
            return;
        }

        championSelect.innerHTML = champions.map(c => {
            const name = c.champion || 'Unknown';
            return `<option value="${escapeHtml(name)}">${escapeHtml(name)}</option>`;
        }).join('');

        // По умолчанию выбираем первого в списке
        championSelect.selectedIndex = 0;

    } catch (err) {
        console.error('Failed to load champion list:', err);
        championSelect.innerHTML = '<option value="" disabled>Error loading champions</option>';
    }
}

/* ─── Search Handler ─── */
async function handleSearch() {
    const champion = document.getElementById('championSelect').value;
    const searchBtn = document.getElementById('searchBtn');

    if (!champion) {
        alert('Please select a champion');
        return;
    }

    setLoadingState(searchBtn, true);

    try {
        const data = await fetchDraftPresence(champion);
        renderPresence(data);
        updateSubtitle(data.champion);
    } catch (err) {
        console.error('Search error:', err);
        alert('Failed to load data. Please try again.');
    } finally {
        setLoadingState(searchBtn, false);
    }
}

/* ─── Async Fetch ─── */
async function fetchDraftPresence(name) {
    const params = new URLSearchParams({
        name: name
    });

    const response = await fetch(`/draft-predict/find/draft-presence?${params.toString()}`, {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    return await response.json();
}

/* ─── Presence Render ─── */
function renderPresence(data) {
    const display = document.getElementById('presenceDisplay');

    if (!data || data.presence == null) {
        display.innerHTML = `
            <div class="presence-empty">
                <p>No presence data found for this champion.</p>
            </div>`;
        return;
    }

    const presenceValue = Number(data.presence);
    const circumference = 2 * Math.PI * 54; // r = 54
    const dashArray = (presenceValue / 100) * circumference;
    const dashOffset = 0;

    display.innerHTML = `
        <div class="presence-value-wrapper">
            <div class="presence-ring">
                <svg class="presence-ring-svg" viewBox="0 0 120 120">
                    <circle class="presence-ring-bg" cx="60" cy="60" r="54"/>
                    <circle class="presence-ring-progress" cx="60" cy="60" r="54"
                            stroke-dasharray="${dashArray} ${circumference}"
                            stroke-dashoffset="${dashOffset}"/>
                </svg>
                <div class="presence-number">
                    <span class="presence-percent">${formatNum(presenceValue)}</span>
                    <span class="presence-symbol">%</span>
                </div>
            </div>
            <div class="presence-label">Pick + Ban Rate</div>
        </div>`;
}

/* ─── Subtitle Update ─── */
function updateSubtitle(championName) {
    const cardSubtitle = document.querySelector('.card-subtitle');
    if (!cardSubtitle) return;
    cardSubtitle.textContent = `Presence for ${escapeHtml(championName || 'Unknown')}`;
}

/* ─── UI Helpers ─── */
function setLoadingState(button, isLoading) {
    if (isLoading) {
        button.dataset.originalText = button.textContent;
        button.textContent = 'Loading...';
        button.disabled = true;
    } else {
        button.textContent = button.dataset.originalText || 'Search';
        button.disabled = false;
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatNum(num) {
    return Number(num).toFixed(1);
}