document.addEventListener('DOMContentLoaded', init);

function init() {
    const champion1Select = document.getElementById('champion1Select');
    const champion2Select = document.getElementById('champion2Select');
    const role1Select = document.getElementById('role1');
    const role2Select = document.getElementById('role2');
    const role3Select = document.getElementById('role3');
    const patchSelect = document.getElementById('patchSelect');
    const searchBtn = document.getElementById('searchBtn');

    if (!champion1Select || !champion2Select || !role1Select || !role2Select || !role3Select || !patchSelect || !searchBtn) {
        console.error('Required DOM elements not found');
        return;
    }

    champion1Select.addEventListener('change', validateChampions);
    champion2Select.addEventListener('change', validateChampions);
    role1Select.addEventListener('change', validateRoles);
    role2Select.addEventListener('change', validateRoles);
    role3Select.addEventListener('change', validateRoles);
    searchBtn.addEventListener('click', handleSearch);
}

function validateChampions() {
    const champion1Select = document.getElementById('champion1Select');
    const champion2Select = document.getElementById('champion2Select');

    if (champion1Select.value && champion2Select.value &&
        champion1Select.value === champion2Select.value) {
        champion2Select.setCustomValidity('Champions must be different');
        return false;
    } else {
        champion2Select.setCustomValidity('');
        return true;
    }
}

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

async function handleSearch() {
    const champion1Select = document.getElementById('champion1Select');
    const champion2Select = document.getElementById('champion2Select');

    if (!champion1Select.value || !champion2Select.value) {
        alert('Please select both champions');
        return;
    }

    if (!validateChampions()) {
        alert('Please select two different champions');
        return;
    }

    if (!validateRoles()) {
        alert('Please select three different roles');
        return;
    }

    const champion1 = champion1Select.value;
    const champion2 = champion2Select.value;
    const role1 = document.getElementById('role1').value;
    const role2 = document.getElementById('role2').value;
    const role3 = document.getElementById('role3').value;
    const patch = document.getElementById('patchSelect').value;
    const searchBtn = document.getElementById('searchBtn');

    setLoadingState(searchBtn, true);

    try {
        const data = await fetchBestTrios(champion1, champion2, role1, role2, role3, patch);
        renderTable(data);
        updateSubtitle(champion1, champion2, role1, role2, role3);
    } catch (err) {
        console.error('Search error:', err);
        alert('Failed to load data. Please try again.');
    } finally {
        setLoadingState(searchBtn, false);
    }
}

async function fetchBestTrios(champion1, champion2, role1, role2, role3, patch) {
    const params = new URLSearchParams({
        champion1: champion1,
        champion2: champion2,
        role1: role1,
        role2: role2,
        role3: role3,
        patch: patch
    });

    const response = await fetch(`/ranked-soloq/find/best-trio?${params.toString()}`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    });

    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    return await response.json();
}

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
                <span class="rate-badge pick">${trio.pickRate}</span>
            </td>
            <td class="col-rate">
                <span class="rate-badge win">${formatNum(trio.winRate)}%</span>
            </td>
        </tr>
    `).join('');
}

function updateSubtitle(champion1, champion2, role1, role2, role3) {
    const cardSubtitle = document.querySelector('.card-subtitle');
    if (!cardSubtitle) return;
    const pretty = (r) => r.charAt(0) + r.slice(1).toLowerCase();
    cardSubtitle.textContent = `${escapeHtml(champion1)} & ${escapeHtml(champion2)} — ${pretty(role1)}, ${pretty(role2)} & ${pretty(role3)} synergies`;
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
    return Number(num).toFixed(1);
}