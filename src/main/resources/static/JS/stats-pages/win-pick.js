document.addEventListener('DOMContentLoaded', init);

/* ─── Initialization ─── */
function init() {
    updateSortIndicator('pick_rate');
    const headers = document.querySelectorAll('.sortable');
    if (headers.length === 0) {
        console.error('Sortable headers not found');
        return;
    }

    headers.forEach(header => {
        header.addEventListener('click', handleSortClick);
    });
}

/* ─── Sort Click Handler ─── */
async function handleSortClick(event) {
    const orderParameter = event.currentTarget.dataset.order;
    if (!orderParameter) return;

    try {
        const data = await fetchWinPick(orderParameter);
        renderTable(data);
        updateSortIndicator(orderParameter);
    } catch (err) {
        console.error('Sort error:', err);
        alert('Failed to load data. Please try again.');
    }
}

/* ─── Async Fetch ─── */
async function fetchWinPick(orderParameter) {
    const params = new URLSearchParams({
        orderParameter: orderParameter
    });

    const response = await fetch(`/draft-predict/find/win-pick?${params.toString()}`, {
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
function renderTable(champions) {
    const tbody = document.getElementById('winPickTableBody');

    if (!Array.isArray(champions) || champions.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4" style="text-align:center;color:var(--text-muted);padding:32px 16px;">
                    No data found.
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
    return Number(num).toFixed(1);
}