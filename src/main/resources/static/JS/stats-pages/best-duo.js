document.addEventListener('DOMContentLoaded', init);

/* ─── Initialization ─── */
function init() {
    const role1Select = document.getElementById('role1');
    const role2Select = document.getElementById('role2');
    const searchBtn = document.getElementById('searchBtn');

    if (!role1Select || !role2Select || !searchBtn) {
        console.error('Required DOM elements not found');
        return;
    }

    role1Select.addEventListener('change', validateRoles);
    role2Select.addEventListener('change', validateRoles);
    searchBtn.addEventListener('click', handleSearch);
}

/* ─── Validation ─── */
function validateRoles() {
    const role1Select = document.getElementById('role1');
    const role2Select = document.getElementById('role2');

    if (role1Select.value === role2Select.value) {
        role2Select.setCustomValidity('Roles must be different');
        return false;
    } else {
        role2Select.setCustomValidity('');
        return true;
    }
}

/* ─── Search Handler ─── */
async function handleSearch() {
    if (!validateRoles()) {
        alert('Please select two different roles');
        return;
    }

    const role1 = document.getElementById('role1').value;
    const role2 = document.getElementById('role2').value;
    const searchBtn = document.getElementById('searchBtn');

    setLoadingState(searchBtn, true);

    try {
        const data = await fetchBestDuos(role1, role2);
        renderTable(data);
        updateSubtitle(role1, role2);
    } catch (err) {
        console.error('Search error:', err);
        alert('Failed to load data. Please try again.');
    } finally {
        setLoadingState(searchBtn, false);
    }
}

/* ─── Async Fetch ─── */
async function fetchBestDuos(role1, role2) {
    const params = new URLSearchParams({
        role1: role1,
        role2: role2
    });

    const response = await fetch(`/draft-predict/find/best-duo?${params.toString()}`, {
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
function renderTable(duos) {
    const tbody = document.getElementById('duoTableBody');

    if (!Array.isArray(duos) || duos.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" style="text-align:center;color:var(--text-muted);padding:32px 16px;">
                    No synergies found for this role combination.
                </td>
            </tr>`;
        return;
    }

    tbody.innerHTML = duos.map((duo, index) => `
        <tr style="animation-delay: ${index * 0.06}s">
            <td class="col-rank">${index + 1}</td>
            <td class="col-champ">
                <div class="champ-cell">
                    <div class="champ-avatar">${escapeHtml(duo.champion1?.charAt(0) || '?')}</div>
                    <span class="champ-name">${escapeHtml(duo.champion1 || 'Unknown')}</span>
                </div>
            </td>
            <td class="col-champ">
                <div class="champ-cell">
                    <div class="champ-avatar">${escapeHtml(duo.champion2?.charAt(0) || '?')}</div>
                    <span class="champ-name">${escapeHtml(duo.champion2 || 'Unknown')}</span>
                </div>
            </td>
            <td class="col-rate">
                <span class="rate-badge pick">${formatNum(duo.pickRate)}%</span>
            </td>
            <td class="col-rate">
                <span class="rate-badge win">${formatNum(duo.winRate)}%</span>
            </td>
        </tr>
    `).join('');
}

/* ─── Subtitle Update ─── */
function updateSubtitle(role1, role2) {
    const cardSubtitle = document.querySelector('.card-subtitle');
    if (!cardSubtitle) return;

    const pretty = (r) => r.charAt(0) + r.slice(1).toLowerCase();
    cardSubtitle.textContent = `${pretty(role1)} & ${pretty(role2)} synergies`;
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