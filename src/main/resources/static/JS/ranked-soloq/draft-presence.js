document.addEventListener('DOMContentLoaded', init);

function init() {
    const patchSelect = document.getElementById('patchSelect');
    const searchBtn = document.getElementById('searchBtn');

    if (!patchSelect || !searchBtn) {
        console.error('Required DOM elements not found');
        return;
    }

    searchBtn.addEventListener('click', handleSearch);
}

async function handleSearch() {
    const patch = document.getElementById('patchSelect').value;
    const searchBtn = document.getElementById('searchBtn');

    if (!patch) {
        alert('Please select a patch');
        return;
    }

    setLoadingState(searchBtn, true);

    try {
        const data = await fetchDraftPresence(patch);
        renderTable(data);
    } catch (err) {
        console.error('Search error:', err);
        alert('Failed to load data. Please try again.');
    } finally {
        setLoadingState(searchBtn, false);
    }
}

async function fetchDraftPresence(patch) {
    const params = new URLSearchParams({ patch: patch });

    const response = await fetch(`/ranked-soloq/find/draft-presence?${params.toString()}`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    });

    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    return await response.json();
}

function renderTable(data) {
    const tbody = document.getElementById('presenceTableBody');

    if (!Array.isArray(data) || data.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="3" style="text-align:center;color:var(--text-muted);padding:32px 16px;">
                    No presence data found for this patch.
                </td>
            </tr>`;
        return;
    }

    tbody.innerHTML = data.map((champ, index) => {
        const name = champ.champion || 'Unknown';
        const presence = champ.presence != null ? Number(champ.presence).toFixed(1) : '0.0';
        const initial = name.charAt(0);

        return `
            <tr style="animation-delay: ${index * 0.05}s">
                <td class="col-rank">${index + 1}</td>
                <td class="col-champ">
                    <div class="champ-cell">
                        <div class="champ-avatar">${escapeHtml(initial)}</div>
                        <span class="champ-name">${escapeHtml(name)}</span>
                    </div>
                </td>
                <td class="col-rate">
                    <span class="rate-badge presence">${escapeHtml(presence)}%</span>
                </td>
            </tr>`;
    }).join('');
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