document.addEventListener('DOMContentLoaded', init);

function init() {
    const champion1Select = document.getElementById('champion1Select');
    const champion2Select = document.getElementById('champion2Select');
    const laneSelect = document.getElementById('laneSelect');
    const patchSelect = document.getElementById('patchSelect');
    const searchBtn = document.getElementById('searchBtn');

    if (!champion1Select || !champion2Select || !laneSelect || !patchSelect || !searchBtn) {
        console.error('Required DOM elements not found');
        return;
    }

    searchBtn.addEventListener('click', handleSearch);
}

async function handleSearch() {
    const champion1 = document.getElementById('champion1Select').value;
    const champion2 = document.getElementById('champion2Select').value;
    const lane = document.getElementById('laneSelect').value;
    const patch = document.getElementById('patchSelect').value;
    const searchBtn = document.getElementById('searchBtn');

    if (!champion1 || !champion2) {
        alert('Please select both champions');
        return;
    }

    if (champion1 === champion2) {
        alert('Please select two different champions');
        return;
    }

    setLoadingState(searchBtn, true);

    try {
        const data = await fetchCounterPick(champion1, champion2, lane, patch);

        if (!data) {
            renderEmpty();
            updateSubtitle(null, null, lane);
            return;
        }

        renderMatchup(data, lane);
        updateSubtitle(data.champion1, data.champion2, lane);
    } catch (err) {
        console.error('Search error:', err);
        alert('Failed to load data. Please try again.');
    } finally {
        setLoadingState(searchBtn, false);
    }
}

async function fetchCounterPick(champion1, champion2, lane, patch) {
    const params = new URLSearchParams({
        champion1: champion1,
        champion2: champion2,
        lane: lane,
        patch: patch
    });

    const response = await fetch(`/ranked-soloq/get/counter-pick?${params.toString()}`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    });

    const text = await response.text();
    if (!text || text.trim() === '') {
        return null;
    }

    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    return JSON.parse(text);
}

function renderEmpty() {
    const display = document.getElementById('matchupDisplay');
    display.innerHTML = `
        <div class="matchup-empty">
            <p>No matchup data found for these champions on this lane.</p>
        </div>`;
}

function renderMatchup(data, lane) {
    const display = document.getElementById('matchupDisplay');

    if (!data || !data.champion1) {
        renderEmpty();
        return;
    }

    const xpClass = data.xp >= 0 ? 'positive' : 'negative';
    const farmClass = data.farm >= 0 ? 'positive' : 'negative';
    const goldClass = data.gold >= 0 ? 'positive' : 'negative';

    const xpSign = data.xp >= 0 ? '+' : '';
    const farmSign = data.farm >= 0 ? '+' : '';
    const goldSign = data.gold >= 0 ? '+' : '';

    const champ1Initial = data.champion1 ? data.champion1.charAt(0) : '?';
    const champ2Initial = data.champion2 ? data.champion2.charAt(0) : '?';

    display.innerHTML = `
        <div class="matchup-vs">
            <div class="champion-side champion-side-left">
                <div class="champion-avatar champ-avatar">${escapeHtml(champ1Initial)}</div>
                <span class="champion-name">${escapeHtml(data.champion1)}</span>
            </div>

            <div class="vs-divider">
                <span class="vs-text">VS</span>
                <span class="vs-lane">${escapeHtml(lane || 'UNKNOWN')}</span>
            </div>

            <div class="champion-side champion-side-right">
                <div class="champion-avatar champ-avatar">${escapeHtml(champ2Initial)}</div>
                <span class="champion-name">${escapeHtml(data.champion2)}</span>
            </div>
        </div>

        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon xp-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M12 2L2 7l10 5 10-5-10-5z"/>
                        <path d="M2 17l10 5 10-5"/>
                        <path d="M2 12l10 5 10-5"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-label">XP Difference</span>
                    <span class="stat-value ${xpClass}">${xpSign}${formatNum(data.xp)}</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon farm-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-label">Farm Difference</span>
                    <span class="stat-value ${farmClass}">${farmSign}${formatNum(data.farm)}</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon gold-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="8" r="7"/>
                        <polyline points="8.21 13.89 7 23 12 20 17 23 15.79 13.88"/>
                    </svg>
                </div>
                <div class="stat-info">
                    <span class="stat-label">Gold Difference</span>
                    <span class="stat-value ${goldClass}">${goldSign}${formatNum(data.gold)}</span>
                </div>
            </div>
        </div>`;
}

function updateSubtitle(champion1, champion2, lane) {
    const cardSubtitle = document.querySelector('.card-subtitle');
    if (!cardSubtitle) return;

    if (!champion1 || !champion2) {
        cardSubtitle.textContent = `No data for ${escapeHtml(lane || 'UNKNOWN')}`;
        return;
    }

    cardSubtitle.textContent = `Matchup: ${escapeHtml(champion1)} vs ${escapeHtml(champion2)} on ${escapeHtml(lane || 'UNKNOWN')}`;
}

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
    if (num === null || num === undefined) return '0.0';
    return Number(num).toFixed(1);
}