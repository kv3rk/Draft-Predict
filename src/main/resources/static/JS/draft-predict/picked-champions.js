const pickedChampionsBlueSide = [];
const pickedChampionsRedSide = [];

function addPickedChampion(championName, side) {
    console.log('[addPickedChampion]', championName, side);
    if (side === 'blue') {
        pickedChampionsBlueSide.push(championName);
    } else {
        pickedChampionsRedSide.push(championName);
    }
    if (typeof window.updateChampionSelectStyles === 'function') {
        window.updateChampionSelectStyles();
    }
}


function getAllPickedChampions() {
    return [...pickedChampionsBlueSide, ...pickedChampionsRedSide];
}

function getPickedChampionsBySide() {
    const result = {
        blueSidePicks: [...pickedChampionsBlueSide],
        redSidePicks: [...pickedChampionsRedSide]
    };
    console.log('[getPickedChampionsBySide]', result);
    return result;
}


function isChampionPicked(championName) {
    return getAllPickedChampions().includes(championName);
}

function resetPickedChampions() {
    pickedChampionsBlueSide.length = 0;
    pickedChampionsRedSide.length = 0;
    if (typeof window.updateChampionSelectStyles === 'function') {
        window.updateChampionSelectStyles();
    }
}

// Экспортируем функции в глобальную область видимости для надежного доступа из других файлов
window.addPickedChampion = addPickedChampion;
window.getAllPickedChampions = getAllPickedChampions;
window.getPickedChampionsBySide = getPickedChampionsBySide;
window.isChampionPicked = isChampionPicked;
window.resetPickedChampions = resetPickedChampions;