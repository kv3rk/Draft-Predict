document.addEventListener('DOMContentLoaded', init);

/* ─── Initialization ─── */
function init() {
    const patchSelect = document.getElementById('patchSelect');
    const searchBtn = document.getElementById('searchBtn');

    if (!patchSelect || !searchBtn) {
        console.error('Required DOM elements not found');
        return;
    }

    loadPatchList();
    searchBtn.addEventListener('click', handleSearch);
}

/* ─── Load Patch List ─── */
async function loadPatchList() {
    const patchSelect = document.getElementById('patchSelect');

    try {
        const response = await fetch('/draft-predict/get/patch-list', {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const patches = await response.json();

        if (!Array.isArray(patches) || patches.length === 0) {
            patchSelect.innerHTML = '<option value="" disabled>No patches found</option>';
            return;
        }

        patchSelect.innerHTML = patches.map(p => {
            const selected = p === 'All patches' ? 'selected' : '';
            return `<option value="${escapeHtml(p)}" ${selected}>${escapeHtml(p)}</option>`;
        }).join('');

    } catch (err) {
        console.error('Failed to load patch list:', err);
        patchSelect.innerHTML = '<option value="" disabled>Error loading patches</option>';
    }
}

/* ─── Search Handler ─── */
async function handleSearch() {
    const patch = document.getElementById('patchSelect').value;
    const searchBtn = document.getElementById('searchBtn');

    if (!patch) {
        alert('Please select a patch');
        return;
    }

    setLoadingState(searchBtn, true);

    try {
        const data = await fetchBanRates(patch);
        renderTable(data);
    } catch (err) {
        console.error('Search error:', err);
        alert('Failed to load data. Please try again.');
    } finally {
        setLoadingState(searchBtn, false);
    }
}

/* ─── Async Fetch ─── */
async function fetchBanRates(patch) {
    const params = new URLSearchParams({ patch: patch });

    const response = await fetch(`/draft-predict/find/ban-rates?${params.toString()}`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    });

    const text = await response.text();
    if (!text || text.trim() === '') {
        return [];
    }

    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    return JSON.parse(text);
}

/* ─── Table Render ─── */
function renderTable(data) {
    const tbody = document.querySelector('.stats-table tbody');

    if (!Array.isArray(data) || data.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="3" class="matchup-empty" style="text-align:center;padding:32px;">
                    No ban rate data found for this patch.
                </td>
            </tr>`;
        return;
    }

    tbody.innerHTML = data.map((champ, index) => {
        const rank = index + 1;
        const name = champ.champion || 'Unknown';
        const banRate = champ.banRate != null ? Number(champ.banRate).toFixed(1) : '0.0';
        const initial = name.charAt(0);

        return `
            <tr style="animation-delay: ${index * 0.05}s">
                <td class="col-rank">${rank}</td>
                <td class="col-champ">
                    <div class="champ-cell">
                        <div class="champ-avatar">${escapeHtml(initial)}</div>
                        <span class="champ-name">${escapeHtml(name)}</span>
                    </div>
                </td>
                <td class="col-rate">
                    <span class="rate-badge ban">${escapeHtml(banRate)}%</span>
                </td>
            </tr>`;
    }).join('');
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