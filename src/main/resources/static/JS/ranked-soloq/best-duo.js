document.addEventListener('DOMContentLoaded', init);
function init() {
    const championSelect = document.getElementById('championSelect');
    const role1Select = document.getElementById('role1');
    const role2Select = document.getElementById('role2');
    const patchSelect = document.getElementById('patchSelect');
    const searchBtn = document.getElementById('searchBtn');
    if (!championSelect || !role1Select || !role2Select || !patchSelect || !searchBtn) {
        console.error('Required DOM elements not found');
        return;
    }
    role1Select.addEventListener('change', validateRoles);
    role2Select.addEventListener('change', validateRoles);
    searchBtn.addEventListener('click', handleSearch);
}
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
async function handleSearch() {
    const championSelect = document.getElementById('championSelect');
    const champion = championSelect.value;
    if (!champion) {
        alert('Please select a champion');
        return;
    }
    if (!validateRoles()) {
        alert('Please select two different roles');
        return;
    }
    const role1 = document.getElementById('role1').value;
    const role2 = document.getElementById('role2').value;
    const patch = document.getElementById('patchSelect').value;
    const searchBtn = document.getElementById('searchBtn');
    setLoadingState(searchBtn, true);
    try {
        const data = await fetchBestDuos(champion, role1, role2, patch);
        renderTable(data);
        updateSubtitle(champion, role1, role2);
    } catch (err) {
        console.error('Search error:', err);
        alert('Failed to load data. Please try again.');
    } finally {
        setLoadingState(searchBtn, false);
    }
}
async function fetchBestDuos(champion, role1, role2, patch) {
    const params = new URLSearchParams({
        champion: champion,
        role1: role1,
        role2: role2,
        patch: patch
    });
    const response = await fetch(`/ranked-soloq/find/best-duo?${params.toString()}`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    });
    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
    return await response.json();
}
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
    tbody.innerHTML = duos.map((duo, index) => {
        const name1 = duo.champion1 || 'Unknown';
        const name2 = duo.champion2 || 'Unknown';
        const logo1 = `/IMG/champions/logo/${escapeHtml(name1)}.png`;
        const logo2 = `/IMG/champions/logo/${escapeHtml(name2)}.png`;
        return `
            <tr style="animation-delay: ${index * 0.06}s">
                <td class="col-rank">${index + 1}</td>
                <td class="col-champ">
                    <div class="champ-cell">
                        <div class="champ-avatar">
                            <img src="${logo1}" alt="${escapeHtml(name1)}" />
                        </div>
                        <span class="champ-name">${escapeHtml(name1)}</span>
                    </div>
                </td>
                <td class="col-champ">
                    <div class="champ-cell">
                        <div class="champ-avatar">
                            <img src="${logo2}" alt="${escapeHtml(name2)}" />
                        </div>
                        <span class="champ-name">${escapeHtml(name2)}</span>
                    </div>
                </td>
                <td class="col-rate">
                    <span class="rate-badge pick">${duo.pickRate}</span>
                </td>
                <td class="col-rate">
                    <span class="rate-badge win">${formatNum(duo.winRate)}%</span>
                </td>
            </tr>
        `;
    }).join('');
}
function updateSubtitle(champion, role1, role2) {
    const cardSubtitle = document.querySelector('.card-subtitle');
    if (!cardSubtitle) return;
    const pretty = (r) => r.charAt(0) + r.slice(1).toLowerCase();
    cardSubtitle.textContent = `${escapeHtml(champion)} — ${pretty(role1)} & ${pretty(role2)} synergies`;
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