document.addEventListener('DOMContentLoaded', init);

/* ─── Initialization ─── */
function init() {
    const role1Select = document.getElementById('role1');
    const role2Select = document.getElementById('role2');
    const role3Select = document.getElementById('role3');
    const searchBtn = document.getElementById('searchBtn');

    if (!role1Select || !role2Select || !role3Select || !searchBtn) {
        console.error('Required DOM elements not found');
        return;
    }

    role1Select.addEventListener('change', validateRoles);
    role2Select.addEventListener('change', validateRoles);
    role3Select.addEventListener('change', validateRoles);
    searchBtn.addEventListener('click', handleSearch);
}

/* ─── Validation ─── */
function validateRoles() {
    const role1Select = document.getElementById('role1');
    const role2Select = document.getElementById('role2');
    const role3Select = document.getElementById('role3');

    const values = [role1Select.value, role2Select.value, role3Select.value];
    const unique = new Set(values);

    if (unique.size !== values.length) {
        role3Select.setCustomValidity('All three roles must be different');
        return false;
    } else {
        role3Select.setCustomValidity('');
        return true;
    }
}

/* ─── Search Handler ─── */
async function handleSearch() {
    if (!validateRoles()) {
        alert('Please select three different roles');
        return;
    }

    const role1 = document.getElementById('role1').value;
    const role2 = document.getElementById('role2').value;
    const role3 = document.getElementById('role3').value;
    const searchBtn = document.getElementById('searchBtn');

    setLoadingState(searchBtn, true);

    try {
        const data = await fetchBestTrios(role1, role2, role3);
        renderTable(data);
        updateSubtitle(role1, role2, role3);
    } catch (err) {
        console.error('Search error:', err);
        alert('Failed to load data. Please try again.');
    } finally {
        setLoadingState(searchBtn, false);
    }
}

/* ─── Async Fetch ─── */
async function fetchBestTrios(role1, role2, role3) {
    const params = new URLSearchParams({
        role1: role1,
        role2: role2,
        role3: role3
    });

    const response = await fetch(`/draft-predict/find/best-trio?${params.toString()}`, {
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
function renderTable(trios) {
    const tbody = document.getElementById('trioTableBody');

    if (!Array.isArray(trios) || trios.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align:center;color:var(--text-muted);padding:32px 16px;">
                    No synergies found for this role combination.
                </td>
            </tr>`;
        return;
    }

    tbody.innerHTML = trios.map((trio, index) => `
        <tr style="animation-delay: ${index * 0.06}s">
            <td class="col-rank">${index + 1}</td>
            <td class="col-champ">
                <div class="champ-cell">
                    <div class="champ-avatar">${escapeHtml(trio.champion1?.charAt(0) || '?')}</div>
                    <span class="champ-name">${escapeHtml(trio.champion1 || 'Unknown')}</span>
                </div>
            </td>
            <td class="col-champ">
                <div class="champ-cell">
                    <div class="champ-avatar">${escapeHtml(trio.champion2?.charAt(0) || '?')}</div>
                    <span class="champ-name">${escapeHtml(trio.champion2 || 'Unknown')}</span>
                </div>
            </td>
            <td class="col-champ">
                <div class="champ-cell">
                    <div class="champ-avatar">${escapeHtml(trio.champion3?.charAt(0) || '?')}</div>
                    <span class="champ-name">${escapeHtml(trio.champion3 || 'Unknown')}</span>
                </div>
            </td>
            <td class="col-rate">
                <span class="rate-badge pick">${formatNum(trio.pickRate)}</span>
            </td>
            <td class="col-rate">
                <span class="rate-badge win">${formatNum(trio.winRate)}%</span>
            </td>
        </tr>
    `).join('');
}

/* ─── Subtitle Update ─── */
function updateSubtitle(role1, role2, role3) {
    const cardSubtitle = document.querySelector('.card-subtitle');
    if (!cardSubtitle) return;

    const pretty = (r) => r.charAt(0) + r.slice(1).toLowerCase();
    cardSubtitle.textContent = `${pretty(role1)}, ${pretty(role2)} & ${pretty(role3)} synergies`;
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