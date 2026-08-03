/* ─── Mock Data ───
   Replace these arrays with fetch() calls to your backend API.
   Expected backend response format matches these structures. */

const winPickData = [
    { rank: 1, name: "Aurora",       emoji: "🌙", winRate: 54.2, pickRate: 12.8 },
    { rank: 2, name: "Skarner",      emoji: "🦂", winRate: 53.7, pickRate: 8.4  },
    { rank: 3, name: "Twitch",       emoji: "🐭", winRate: 52.9, pickRate: 15.2 },
    { rank: 4, name: "Karthus",      emoji: "💀", winRate: 52.4, pickRate: 6.1  },
    { rank: 5, name: "Shyvana",      emoji: "🐉", winRate: 51.8, pickRate: 4.3  },
    { rank: 6, name: "Swain",        emoji: "🦅", winRate: 51.5, pickRate: 5.7  },
    { rank: 7, name: "Brand",        emoji: "🔥", winRate: 51.2, pickRate: 9.9  },
    { rank: 8, name: "Malzahar",     emoji: "👁",  winRate: 50.9, pickRate: 3.2  },
];

const banData = [
    { rank: 1, name: "Yasuo",        emoji: "⚔",  banRate: 38.5 },
    { rank: 2, name: "Zed",          emoji: "🗡",  banRate: 34.2 },
    { rank: 3, name: "Yone",         emoji: "🌪",  banRate: 31.7 },
    { rank: 4, name: "Master Yi",    emoji: "⚡",  banRate: 28.4 },
    { rank: 5, name: "Vayne",        emoji: "🏹", banRate: 25.1 },
    { rank: 6, name: "Darius",       emoji: "🪓", banRate: 22.8 },
    { rank: 7, name: "Morgana",      emoji: "🔗", banRate: 19.3 },
    { rank: 8, name: "Blitzcrank",   emoji: "🤖", banRate: 17.6 },
];

/* Total games — this value comes from backend */
const TOTAL_GAMES = 124_857;

/* ─── DOM Elements ─── */
const winPickBody = document.getElementById('winPickBody');
const banBody     = document.getElementById('banBody');
const totalGames  = document.getElementById('totalGames');

/* ─── Render Helpers ─── */
function createChampCell(champ) {
    return `
        <div class="champ-cell">
            <div class="champ-avatar">${champ.emoji}</div>
            <span class="champ-name">${champ.name}</span>
        </div>
    `;
}

function createRateBadge(value, type) {
    const cls = type === 'win' ? 'win' : type === 'pick' ? 'pick' : 'ban';
    return `<span class="rate-badge ${cls}">${value.toFixed(1)}%</span>`;
}

function createBar(value, type) {
    const cls = type === 'win' ? 'win' : type === 'pick' ? 'pick' : 'ban';
    const pct = Math.min(value, 100);
    return `
        <div class="bar-track">
            <div class="bar-fill ${cls}" data-width="${pct}"></div>
        </div>
    `;
}

/* ─── Render Tables ─── */
function renderWinPick() {
    winPickBody.innerHTML = winPickData.map((champ, i) => `
        <tr style="animation-delay: ${0.4 + i * 0.06}s">
            <td class="col-rank">${champ.rank}</td>
            <td class="col-champ">${createChampCell(champ)}</td>
            <td class="col-rate">${createRateBadge(champ.winRate, 'win')}</td>
            <td class="col-rate">${createRateBadge(champ.pickRate, 'pick')}</td>
        </tr>
    `).join('');
}

function renderBans() {
    banBody.innerHTML = banData.map((champ, i) => `
        <tr style="animation-delay: ${0.55 + i * 0.06}s">
            <td class="col-rank">${champ.rank}</td>
            <td class="col-champ">${createChampCell(champ)}</td>
            <td class="col-rate">${createRateBadge(champ.banRate, 'ban')}</td>
            <td class="col-bar">${createBar(champ.banRate, 'ban')}</td>
        </tr>
    `).join('');
}

/* ─── Animate Progress Bars ─── */
function animateBars() {
    const bars = document.querySelectorAll('.bar-fill');
    bars.forEach(bar => {
        const width = bar.dataset.width;
        // small delay to let the row fade-in start first
        setTimeout(() => {
            bar.style.width = width + '%';
        }, 100);
    });
}

/* ─── Animated Number Counter ─── */
function animateCounter(element, target, duration = 1500) {
    const start = 0;
    const startTime = performance.now();

    function update(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);

        // easeOutExpo
        const eased = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress);
        const current = Math.floor(start + (target - start) * eased);

        element.textContent = current.toLocaleString('en-US');

        if (progress < 1) {
            requestAnimationFrame(update);
        } else {
            // Pop effect on finish
            element.classList.add('pop');
            setTimeout(() => element.classList.remove('pop'), 300);
        }
    }

    requestAnimationFrame(update);
}

/* ─── Initialization ─── */
function init() {
    renderWinPick();
    renderBans();

    // Animate bars after a brief delay so CSS transitions catch
    setTimeout(animateBars, 300);

    // Animate total games counter
    setTimeout(() => {
        animateCounter(totalGames, TOTAL_GAMES);
    }, 600);
}

/* ─── Fetch Integration Stub ───
   Uncomment and adapt when backend is ready:

async function loadFromBackend() {
    try {
        const [winRes, banRes, metaRes] = await Promise.all([
            fetch('/api/champions/win-pick'),
            fetch('/api/champions/bans'),
            fetch('/api/meta')
        ]);

        const winData = await winRes.json();
        const banData = await banRes.json();
        const meta    = await metaRes.json();

        // update global data and re-render
        // winPickData = winData;
        // banData     = banData;
        // TOTAL_GAMES = meta.totalGames;

        init();
    } catch (err) {
        console.error('Failed to load stats:', err);
    }
}
*/

// Boot
document.addEventListener('DOMContentLoaded', init);