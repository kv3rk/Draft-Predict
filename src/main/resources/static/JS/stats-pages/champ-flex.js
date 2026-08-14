document.addEventListener('DOMContentLoaded', init);

/* ─── Initialization ─── */
function init() {
    const championSelect = document.getElementById('championSelect');
    const patchSelect = document.getElementById('patchSelect');
    const searchBtn = document.getElementById('searchBtn');

    if (!championSelect || !patchSelect || !searchBtn) {
        console.error('Required DOM elements not found');
        return;
    }

    loadChampionList();
    loadPatchList();
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

        championSelect.selectedIndex = 0;

    } catch (err) {
        console.error('Failed to load champion list:', err);
        championSelect.innerHTML = '<option value="" disabled>Error loading champions</option>';
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
    const champion = document.getElementById('championSelect').value;
    const patch = document.getElementById('patchSelect').value;
    const searchBtn = document.getElementById('searchBtn');

    if (!champion) {
        alert('Please select a champion');
        return;
    }

    setLoadingState(searchBtn, true);

    try {
        const data = await fetchChampFlex(champion, patch);
        renderTable(data);
        updateSubtitle(data.champion);
    } catch (err) {
        console.error('Search error:', err);
        alert('Failed to load data. Please try again.');
    } finally {
        setLoadingState(searchBtn, false);
    }
}

/* ─── Async Fetch ─── */
async function fetchChampFlex(name, patch) {
    const params = new URLSearchParams({
        name: name,
        patch: patch
    });

    const response = await fetch(`/draft-predict/find/champ-flex?${params.toString()}`, {
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

/* ─── Table Render ─── */
function renderTable(data) {
    const tbody = document.getElementById('flexTableBody');

    if (!data) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" style="text-align:center;color:var(--text-muted);padding:32px 16px;">
                    No flexibility data found for this champion.
                </td>
            </tr>`;
        return;
    }

    const roles = [
        { key: 'top', name: 'TOP' },
        { key: 'jungle', name: 'JUNGLE' },
        { key: 'middle', name: 'MIDDLE' },
        { key: 'bottom', name: 'BOTTOM' },
        { key: 'utility', name: 'UTILITY' }
    ];

    const rows = roles
        .map((role, index) => {
            const value = data[role.key];
            if (value == null) return null;
            return { ...role, value, index };
        })
        .filter(r => r !== null);

    if (rows.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" style="text-align:center;color:var(--text-muted);padding:32px 16px;">
                    No flexibility data found for this champion.
                </td>
            </tr>`;
        return;
    }

    tbody.innerHTML = rows.map((row, idx) => `
        <tr style="animation-delay: ${(idx + 1) * 0.06}s">
            <td class="col-rank">${idx + 1}</td>
            <td class="col-role">
                <div class="role-cell">
                    <span class="role-badge">${escapeHtml(row.name)}</span>
                </div>
            </td>
            <td class="col-rate">
                <span class="rate-badge pick">${formatNum(row.value)}%</span>
            </td>
            <td class="col-bar">
                <div class="bar-track">
                    <div class="bar-fill pick" style="width: ${formatNum(row.value)}%"></div>
                </div>
            </td>
        </tr>
    `).join('');
}

/* ─── Subtitle Update ─── */
function updateSubtitle(championName) {
    const cardSubtitle = document.querySelector('.card-subtitle');
    if (!cardSubtitle) return;
    cardSubtitle.textContent = `Flexibility for ${escapeHtml(championName || 'Unknown')}`;
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