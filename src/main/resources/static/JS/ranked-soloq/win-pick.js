document.addEventListener('DOMContentLoaded', init);

/* ─── Initialization ─── */
function init() {
    const patchSelect = document.getElementById('patchSelect');
    const searchBtn = document.getElementById('searchBtn');
    const headers = document.querySelectorAll('.sortable');

    if (!patchSelect || !searchBtn) {
        console.error('Required DOM elements not found');
        return;
    }

    loadPatchList();
    searchBtn.addEventListener('click', handleSearch);

    if (headers.length > 0) {
        headers.forEach(header => {
            header.addEventListener('click', handleSortClick);
        });
    }
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
    const activeHeader = document.querySelector('.sortable.active');
    const orderParameter = activeHeader ? activeHeader.dataset.order : 'pick_rate';

    await loadData(orderParameter, patch);
}

/* ─── Sort Click Handler ─── */
async function handleSortClick(event) {
    const orderParameter = event.currentTarget.dataset.order;
    if (!orderParameter) return;

    const patch = document.getElementById('patchSelect').value || 'All patches';

    await loadData(orderParameter, patch);
}

/* ─── Load Data (fetch + render + indicator) ─── */
async function loadData(orderParameter, patch) {
    try {
        const data = await fetchWinPick(orderParameter, patch);
        renderTable(data);
        updateSortIndicator(orderParameter);
    } catch (err) {
        console.error('Load error:', err);
        alert('Failed to load data. Please try again.');
    }
}

/* ─── Async Fetch ─── */
async function fetchWinPick(orderParameter, patch) {
    const params = new URLSearchParams({
        orderParameter: orderParameter,
        patch: patch
    });

    const response = await fetch(`/draft-predict/find/win-pick?${params.toString()}`, {
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
function renderTable(champions) {
    const tbody = document.getElementById('winPickTableBody');

    if (!Array.isArray(champions) || champions.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" style="text-align:center;color:var(--text-muted);padding:32px 16px;">
                    No data found for this patch.
                </td>
            </tr>`;
        return;
    }

    tbody.innerHTML = champions.map((champ, index) => `
        <tr style="animation-delay: ${index * 0.06}s">
            <td class="col-rank">${index + 1}</td>
            <td class="col-champ">
                <div class="champ-cell">
                    <div class="champ-avatar">${escapeHtml(champ.champion?.charAt(0) || '?')}</div>
                    <span class="champ-name">${escapeHtml(champ.champion || 'Unknown')}</span>
                </div>
            </td>
            <td class="col-rate">
                <span class="rate-badge win">${formatNum(champ.winRate)}%</span>
            </td>
            <td class="col-rate">
                <span class="rate-badge pick">${formatNum(champ.pickRate)}%</span>
            </td>
        </tr>
    `).join('');
}

/* ─── Sort Indicator ─── */
function updateSortIndicator(activeOrder) {
    const headers = document.querySelectorAll('.sortable');
    headers.forEach(header => {
        if (header.dataset.order === activeOrder) {
            header.classList.add('active');
        } else {
            header.classList.remove('active');
        }
    });
}

/* ─── Helpers ─── */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatNum(num) {
    if (num === null || num === undefined) return '0.0';
    return Number(num).toFixed(1);
}