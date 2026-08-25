document.addEventListener('DOMContentLoaded', init);

function init() {
    const championSelect = document.getElementById('championSelect');
    const laneSelect = document.getElementById('laneSelect');
    const patchSelect = document.getElementById('patchSelect');
    const searchBtn = document.getElementById('searchBtn');

    if (!championSelect || !laneSelect || !patchSelect || !searchBtn) {
        console.error('Required DOM elements not found');
        return;
    }

    searchBtn.addEventListener('click', handleSearch);
}

async function handleSearch() {
    const champion = document.getElementById('championSelect').value;
    const lane = document.getElementById('laneSelect').value;
    const patch = document.getElementById('patchSelect').value;
    const searchBtn = document.getElementById('searchBtn');

    if (!champion) {
        alert('Please select a champion');
        return;
    }

    setLoadingState(searchBtn, true);

    try {
        const data = await fetchCounterPick(champion, lane, patch);
        renderTable(data);
    } catch (err) {
        console.error('Search error:', err);
        alert('Failed to load data. Please try again.');
    } finally {
        setLoadingState(searchBtn, false);
    }
}

async function fetchCounterPick(champion, lane, patch) {
    const params = new URLSearchParams({
        champion1: champion,
        lane: lane,
        patch: patch
    });

    const response = await fetch(`/ranked-soloq/get/counter-pick?${params.toString()}`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    });

    if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    return await response.json();
}

function renderTable(data) {
    const tbody = document.getElementById('counterTableBody');
    if (!Array.isArray(data) || data.length === 0) {
        tbody.innerHTML = `
         <tr>
             <td colspan="5" style="text-align:center;color:var(--text-muted);padding:32px 16px;">
                 No counter pick data found for this champion.
             </td>
         </tr>`;
        return;
    }
    tbody.innerHTML = data.map((counter, index) => {
        const name = counter.champion2 || 'Unknown';
        const xp = counter.xp != null ? Number(counter.xp).toFixed(1) : '0.0';
        const farm = counter.farm != null ? Number(counter.farm).toFixed(1) : '0.0';
        const gold = counter.gold != null ? Number(counter.gold).toFixed(1) : '0.0';
        const initial = name.charAt(0);
        const xpClass = counter.xp >= 0 ? 'positive' : 'negative';
        const farmClass = counter.farm >= 0 ? 'positive' : 'negative';
        const goldClass = counter.gold >= 0 ? 'positive' : 'negative';
        const xpSign = counter.xp >= 0 ? '+' : '';
        const farmSign = counter.farm >= 0 ? '+' : '';
        const goldSign = counter.gold >= 0 ? '+' : '';
        const logoSrc = `/IMG/champions/logo/${escapeHtml(name)}.png`;

        return `
         <tr style="animation-delay: ${index * 0.05}s">
             <td class="col-rank">${index + 1}</td>
             <td class="col-champ">
                 <div class="champ-cell">
                     <div class="champ-avatar">
                         <img src="${logoSrc}" alt="${escapeHtml(name)}"
                              onerror="this.onerror=null;this.remove();this.parentElement.insertAdjacentHTML('beforeend','<span class=\\'champ-avatar-fallback\\'>${escapeHtml(initial)}</span>');" />
                     </div>
                     <span class="champ-name">${escapeHtml(name)}</span>
                 </div>
             </td>
             <td class="col-rate">
                 <span class="rate-badge gold ${goldClass}">${goldSign}${escapeHtml(gold)}</span>
             </td>
             <td class="col-rate">
                 <span class="rate-badge xp ${xpClass}">${xpSign}${escapeHtml(xp)}</span>
             </td>
             <td class="col-rate">
                 <span class="rate-badge farm ${farmClass}">${farmSign}${escapeHtml(farm)}</span>
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