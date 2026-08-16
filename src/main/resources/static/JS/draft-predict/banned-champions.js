const bannedChampionsBlueSide = [];
const bannedChampionsRedSide = [];

function addBannedChampion(championName, side) {
    if (side === 'blue') {
        bannedChampionsBlueSide.push(championName);
    } else {
        bannedChampionsRedSide.push(championName);
    }
    updateChampionSelectStyles();
}

function getAllBannedChampions() {
    return [...bannedChampionsBlueSide, ...bannedChampionsRedSide];
}

function getBannedChampionsBySide() {
    return {
        blueSideBans: [...bannedChampionsBlueSide],
        redSideBans: [...bannedChampionsRedSide]
    };
}

function isChampionBanned(championName) {
    return getAllBannedChampions().includes(championName);
}

function updateChampionSelectStyles() {
    const select = document.getElementById('championSelect');
    if (!select) return;

    const options = select.querySelectorAll('option');
    options.forEach(option => {
        if (!option.value) return;
        if (isChampionBanned(option.value)) {
            option.classList.add('banned-option');
            option.classList.remove('available-option');
        } else {
            option.classList.add('available-option');
            option.classList.remove('banned-option');
        }
    });
}

function resetBannedChampions() {
    bannedChampionsBlueSide.length = 0;
    bannedChampionsRedSide.length = 0;
    updateChampionSelectStyles();
}

function showToast(message) {
    let toast = document.getElementById('draftToast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'draftToast';
        toast.className = 'draft-toast';
        document.body.appendChild(toast);
    }

    toast.textContent = message;
    toast.classList.add('show');

    setTimeout(() => {
        toast.classList.remove('show');
    }, 2500);
}

window.addBannedChampion = addBannedChampion;
window.getAllBannedChampions = getAllBannedChampions;
window.getBannedChampionsBySide = getBannedChampionsBySide;
window.isChampionBanned = isChampionBanned;
window.resetBannedChampions = resetBannedChampions;
window.showToast = showToast;