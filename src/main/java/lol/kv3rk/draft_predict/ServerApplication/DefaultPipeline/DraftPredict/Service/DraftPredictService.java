package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DraftPredict.Service;

import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DraftPredict.DTO.DraftContext;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.DTO.*;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.Repository.*;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.DTO.TopPerformingChampions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class DraftPredictService {

    private final ChampionFlexibilityRequests championFlexibilityRequests;
    private final CounterPickRequests counterPickRequests;
    private final WinRateRequests winRateRequests;
    private final PickRateRequests pickRateRequests;
    private final BanRateRequests banRateRequests;
    private final DraftPresenceRequests draftPresenceRequests;
    private final BestDuoRequests bestDuoRequests;
    private final BestTrioRequests bestTrioRequests;

    private String currentPatchFilter = "%";
    private String currentPatch = "";


    private List<String> cachedBanRatesByActualPatch;
    private List<String> cachedDraftPresenceByActualPatch;
    private List<String> cachedWinRatesByActualPatch;
    private List<String> cachedPickRatesByActualPatch;

    private final Map<String, ChampionFlexibility> flexibilityCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> counterPicksCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> bestDuoCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> bestTrioCache = new ConcurrentHashMap<>();

    // Hard occupied - железобетонно занятые позиции
    private final Set<String> blueSideOccupiedPositions = ConcurrentHashMap.newKeySet();
    private final Set<String> redSideOccupiedPositions = ConcurrentHashMap.newKeySet();

    // Soft occupied - вероятные позиции для флекс-чемпионов
    private final Set<String> blueSideSoftOccupiedPositions = ConcurrentHashMap.newKeySet();
    private final Set<String> redSideSoftOccupiedPositions = ConcurrentHashMap.newKeySet();

    public DraftPredictService(ChampionFlexibilityRequests championFlexibilityRequests,
                               CounterPickRequests counterPickRequests,
                               WinRateRequests winRateRequests,
                               PickRateRequests pickRateRequests,
                               BanRateRequests banRateRequests,
                               DraftPresenceRequests draftPresenceRequests,
                               BestDuoRequests bestDuoRequests,
                               BestTrioRequests bestTrioRequests) {

        this.championFlexibilityRequests = championFlexibilityRequests;
        this.counterPickRequests = counterPickRequests;
        this.winRateRequests = winRateRequests;
        this.pickRateRequests = pickRateRequests;
        this.banRateRequests = banRateRequests;
        this.draftPresenceRequests = draftPresenceRequests;
        this.bestDuoRequests = bestDuoRequests;
        this.bestTrioRequests = bestTrioRequests;
    }

    public void setupMatchInfo(String patchFilter, String patch) {
        log.info("Setting up match info - PatchFilter: {}, Patch: {}", patchFilter, patch);
        this.currentPatchFilter = patchFilter;
        this.currentPatch = patch;
        rebuildCache();
    }

    private void rebuildCache() {
        log.info("Rebuilding draft predict statistics cache for patch filter: {}", currentPatchFilter);

        cachedBanRatesByActualPatch = banRateRequests.getMostBannedChampionsByActualPatch(currentPatch)
                .stream().map(MostBannedChampions::getChampion).toList();
        cachedDraftPresenceByActualPatch = draftPresenceRequests.getChampionDraftPresenceByActualPatch(currentPatch)
                .stream().map(ChampionPresence::getChampion).toList();
        cachedPickRatesByActualPatch = pickRateRequests.getPickRateByActualPatch(currentPatch)
                .stream().map(TopPerformingChampions::getChampion).toList();
        cachedWinRatesByActualPatch = winRateRequests.getWinRateByActualPatch(currentPatch)
                .stream().map(TopPerformingChampions::getChampion).toList();
        counterPicksCache.clear();
        bestDuoCache.clear();
        bestTrioCache.clear();
        log.info("Rebuilt draft predict statistics cache");
    }

    private DraftContext prepareDraftContext(List<String> blueSideBans,
                                             List<String> redSideBans,
                                             List<String> blueSidePicks,
                                             List<String> redSidePicks) {
        updateOccupiedPositions(blueSidePicks, redSidePicks);
        Set<String> excludedChampions = Stream.of(
                blueSideBans.stream(),
                redSideBans.stream(),
                blueSidePicks.stream(),
                redSidePicks.stream()
        ).flatMap(s -> s).collect(Collectors.toSet());
        return new DraftContext(excludedChampions);
    }

    // ================= PUBLIC API (RECOMMENDATIONS) =================

    public List<String> getBlueSideBanRecommendations(List<String> blueSideBans,
                                                      List<String> redSideBans,
                                                      List<String> blueSidePicks,
                                                      List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);

        Set<String> allHardOccupied = new HashSet<>(blueSideOccupiedPositions);
        allHardOccupied.addAll(redSideOccupiedPositions);

        Set<String> allSoftOccupied = new HashSet<>(blueSideSoftOccupiedPositions);
        allSoftOccupied.addAll(redSideSoftOccupiedPositions);

        // Вариант B: генерируем рекомендации для разных сценариев
        Set<String> recommendations = new LinkedHashSet<>();

        // Сценарий 1: базовый (без учёта soft occupied)
        recommendations.addAll(generateBanRecommendations(context, allHardOccupied, redSidePicks));

        // Сценарий 2: для каждой soft-роли генерируем отдельно
        for (String softRole : allSoftOccupied) {
            Set<String> scenarioOccupied = new HashSet<>(allHardOccupied);
            scenarioOccupied.add(softRole);
            recommendations.addAll(generateBanRecommendations(context, scenarioOccupied, redSidePicks));
        }

        return recommendations.stream().limit(5).toList();
    }

    public List<String> getRedSideBanRecommendations(List<String> blueSideBans,
                                                     List<String> redSideBans,
                                                     List<String> blueSidePicks,
                                                     List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);

        Set<String> allHardOccupied = new HashSet<>(blueSideOccupiedPositions);
        allHardOccupied.addAll(redSideOccupiedPositions);

        Set<String> allSoftOccupied = new HashSet<>(blueSideSoftOccupiedPositions);
        allSoftOccupied.addAll(redSideSoftOccupiedPositions);

        // Вариант B: генерируем рекомендации для разных сценариев
        Set<String> recommendations = new LinkedHashSet<>();

        // Сценарий 1: базовый (без учёта soft occupied)
        recommendations.addAll(generateBanRecommendations(context, allHardOccupied, blueSidePicks));

        // Сценарий 2: для каждой soft-роли генерируем отдельно
        for (String softRole : allSoftOccupied) {
            Set<String> scenarioOccupied = new HashSet<>(allHardOccupied);
            scenarioOccupied.add(softRole);
            recommendations.addAll(generateBanRecommendations(context, scenarioOccupied, blueSidePicks));
        }

        return recommendations.stream().limit(5).toList();
    }

    public List<String> getBlueSidePickRecommendations(List<String> blueSideBans,
                                                       List<String> redSideBans,
                                                       List<String> blueSidePicks,
                                                       List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);

        // Вариант B: генерируем рекомендации для разных сценариев
        Set<String> recommendations = new LinkedHashSet<>();

        // Сценарий 1: базовый (без учёта soft occupied)
        recommendations.addAll(generateBlueSidePickRecommendations(context, blueSideOccupiedPositions,
                blueSidePicks, redSidePicks, blueSideBans, redSideBans));

        // Сценарий 2: для каждой soft-роли генерируем отдельно
        for (String softRole : blueSideSoftOccupiedPositions) {
            Set<String> scenarioOccupied = new HashSet<>(blueSideOccupiedPositions);
            scenarioOccupied.add(softRole);
            recommendations.addAll(generateBlueSidePickRecommendations(context, scenarioOccupied,
                    blueSidePicks, redSidePicks, blueSideBans, redSideBans));
        }

        return recommendations.stream().limit(5).toList();
    }

    public List<String> getRedSidePickRecommendations(List<String> blueSideBans,
                                                      List<String> redSideBans,
                                                      List<String> blueSidePicks,
                                                      List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);

        // Вариант B: генерируем рекомендации для разных сценариев
        Set<String> recommendations = new LinkedHashSet<>();

        // Сценарий 1: базовый (без учёта soft occupied)
        recommendations.addAll(generateRedSidePickRecommendations(context, redSideOccupiedPositions,
                blueSidePicks, redSidePicks, blueSideBans, redSideBans));

        // Сценарий 2: для каждой soft-роли генерируем отдельно
        for (String softRole : redSideSoftOccupiedPositions) {
            Set<String> scenarioOccupied = new HashSet<>(redSideOccupiedPositions);
            scenarioOccupied.add(softRole);
            recommendations.addAll(generateRedSidePickRecommendations(context, scenarioOccupied,
                    blueSidePicks, redSidePicks, blueSideBans, redSideBans));
        }

        return recommendations.stream().limit(5).toList();
    }

    // ================= PRIVATE GENERATION METHODS (Вариант B) =================

    private List<String> generateBanRecommendations(DraftContext context, Set<String> occupiedPositions,
                                                    List<String> opponentPicks) {
        Map<String, Integer> freq = new HashMap<>();

        List<String> bestDuos = getBestDuoList(opponentPicks);
        for (String c : bestDuos) {
            if (!context.excludedChampions().contains(c)) freq.merge(c, 1, Integer::sum);
        }

        List<String> bestTrios = getBestTrioList(opponentPicks);
        for (String c : bestTrios) {
            if (!context.excludedChampions().contains(c)) freq.merge(c, 1, Integer::sum);
        }

        int totalBans = context.excludedChampions().size(); // упрощённо
        Map<String, Integer> phaseFreq = Map.of();
        if (totalBans < 6) {
            phaseFreq = getEarlyPhaseBanRecommendations(context.excludedChampions(), List.of());
        }

        for (Map.Entry<String, Integer> entry : phaseFreq.entrySet()) {
            freq.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        freq.entrySet().removeIf(entry -> isChampionBlockedByOccupiedPositions(entry.getKey(), occupiedPositions));

        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<String> generateBlueSidePickRecommendations(DraftContext context, Set<String> occupiedPositions,
                                                             List<String> blueSidePicks, List<String> redSidePicks,
                                                             List<String> blueSideBans, List<String> redSideBans) {
        Map<String, Integer> freq = new HashMap<>();

        List<String> bestDuos = getBestDuoList(blueSidePicks);
        for (String c : bestDuos) {
            if (!context.excludedChampions().contains(c)) freq.merge(c, 1, Integer::sum);
        }

        List<String> bestTrios = getBestTrioList(blueSidePicks);
        for (String c : bestTrios) {
            if (!context.excludedChampions().contains(c)) freq.merge(c, 1, Integer::sum);
        }

        List<String> counterPicks = getCounterPickListForOppositePicks(redSidePicks);
        for (String c : counterPicks) {
            if (!context.excludedChampions().contains(c)) freq.merge(c, 1, Integer::sum);
        }

        int totalPicks = blueSidePicks.size() + redSidePicks.size();
        Map<String, Integer> phaseFreq = Map.of();
        if (totalPicks < 6) {
            if (blueSidePicks.isEmpty()) {
                phaseFreq = getFirstPickRecommendation(context.excludedChampions(), blueSideBans, redSideBans);
            }
        }

        for (Map.Entry<String, Integer> entry : phaseFreq.entrySet()) {
            freq.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        freq.entrySet().removeIf(entry -> isChampionBlockedByOccupiedPositions(entry.getKey(), occupiedPositions));

        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<String> generateRedSidePickRecommendations(DraftContext context, Set<String> occupiedPositions,
                                                            List<String> blueSidePicks, List<String> redSidePicks,
                                                            List<String> blueSideBans, List<String> redSideBans) {
        Map<String, Integer> freq = new HashMap<>();

        List<String> bestDuos = getBestDuoList(redSidePicks);
        for (String c : bestDuos) {
            if (!context.excludedChampions().contains(c)) freq.merge(c, 1, Integer::sum);
        }

        List<String> bestTrios = getBestTrioList(redSidePicks);
        for (String c : bestTrios) {
            if (!context.excludedChampions().contains(c)) freq.merge(c, 1, Integer::sum);
        }

        List<String> counterPicks = getCounterPickListForOppositePicks(blueSidePicks);
        for (String c : counterPicks) {
            if (!context.excludedChampions().contains(c)) freq.merge(c, 1, Integer::sum);
        }

        int totalPicks = blueSidePicks.size() + redSidePicks.size();
        Map<String, Integer> phaseFreq = Map.of();
        if (totalPicks < 6) {
            if (redSidePicks.isEmpty()) {
                phaseFreq = getFirstPickRecommendation(context.excludedChampions(), blueSidePicks, redSidePicks);
            }
        }

        for (Map.Entry<String, Integer> entry : phaseFreq.entrySet()) {
            freq.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        freq.entrySet().removeIf(entry -> isChampionBlockedByOccupiedPositions(entry.getKey(), occupiedPositions));

        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }

    // ================= PRIVATE PHASE METHODS =================

    private Map<String, Integer> getFirstPickRecommendation(Set<String> excludedChampions, List<String> blueSideBans, List<String> redSideBans) {
        Map<String, Integer> freq = new HashMap<>(getGeneralFrequencyMap(excludedChampions));
        List<String> counterPicksBlueSideBans = getCounterPickListForBans(blueSideBans);
        for (String c : counterPicksBlueSideBans) {
            if (!excludedChampions.contains(c)) freq.merge(c, 1, Integer::sum);
        }
        List<String> counterPicksRedSideBans = getCounterPickListForBans(redSideBans);
        for (String c : counterPicksRedSideBans) {
            if (!excludedChampions.contains(c)) freq.merge(c, 1, Integer::sum);
        }
        return freq;
    }

    private Map<String, Integer> getEarlyPhaseBanRecommendations(Set<String> excludedChampions, List<String> banList) {
        Map<String, Integer> freq = new HashMap<>(getGeneralFrequencyMap(excludedChampions));
        List<String> counterPicks = getCounterPickListForBans(banList);
        for (String c : counterPicks) {
            if (!excludedChampions.contains(c)) freq.merge(c, 1, Integer::sum);
        }
        return freq;
    }

    // ================= PRIVATE HELPER METHODS =================

    private Map<String, Integer> getGeneralFrequencyMap(Set<String> excludedChampions) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        Stream.of(cachedBanRatesByActualPatch, cachedDraftPresenceByActualPatch, cachedPickRatesByActualPatch, cachedWinRatesByActualPatch)
                .flatMap(List::stream)
                .filter(champion -> !excludedChampions.contains(champion))
                .forEach(champion -> frequencyMap.merge(champion, 1, Integer::sum));
        return frequencyMap;
    }

    private List<String> getBestDuoList(List<String> champions) {
        List<String> allBestDuos = new ArrayList<>();
        for (String champion : champions) {
            String cacheKey = "duo:" + champion + ":" + currentPatchFilter;
            List<String> duos = bestDuoCache.computeIfAbsent(cacheKey,
                    key -> bestDuoRequests.getBestDuoChampionsWithoutRoleConstraint(currentPatchFilter, champion)
                            .stream().map(BestDuo::getChampion2).toList()
            );
            allBestDuos.addAll(duos);
        }
        return allBestDuos;
    }

    private List<String> getBestTrioList(List<String> champions) {
        List<String> allBestTrios = new ArrayList<>();
        for (int i = 0; i < champions.size(); i++) {
            for (int j = i + 1; j < champions.size(); j++) {
                String champ1 = champions.get(i);
                String champ2 = champions.get(j);
                String normalizedKey = champ1.compareTo(champ2) <= 0
                        ? "trio:" + champ1 + ":" + champ2 + ":" + currentPatchFilter
                        : "trio:" + champ2 + ":" + champ1 + ":" + currentPatchFilter;
                List<String> trios = bestTrioCache.computeIfAbsent(normalizedKey,
                        key -> bestTrioRequests.getBestTrioChampionsNoRole(currentPatchFilter, champ1, champ2)
                                .stream().map(BestTrio::getChampion3).toList()
                );
                allBestTrios.addAll(trios);
            }
        }
        return allBestTrios;
    }

    private List<String> getCounterPickListForBans(List<String> champions) {
        List<String> allCounterPicks = new ArrayList<>();
        for (String champion : champions) {
            ChampionFlexibility flex = getFlexibility(champion);
            if (flex == null) continue;
            List<String> validRoles = getRoles(flex);
            for (String role : validRoles) {
                String cacheKey = champion + ":" + role + ":" + currentPatchFilter;
                List<String> counters = counterPicksCache.computeIfAbsent(cacheKey,
                        key -> counterPickRequests.getBestMatchups(champion, role, currentPatchFilter)
                                .stream().map(CounterPick::getChampion2).toList()
                );
                allCounterPicks.addAll(counters);
            }
        }
        return allCounterPicks;
    }

    private List<String> getCounterPickListForOppositePicks(List<String> champions) {
        List<String> allCounterPicks = new ArrayList<>();
        for (String champion : champions) {
            ChampionFlexibility flex = getFlexibility(champion);
            if (flex == null) continue;
            List<String> validRoles = getRoles(flex);
            for (String role : validRoles) {
                String cacheKey = champion + ":" + role + ":" + currentPatchFilter;
                List<String> counters = counterPickRequests.getWorstMatchups(champion, role, currentPatchFilter)
                        .stream().map(CounterPick::getChampion2).toList();
                allCounterPicks.addAll(counters);
            }
        }
        return allCounterPicks;
    }

    private void updateOccupiedPositions(List<String> blueSidePicks, List<String> redSidePicks) {
        blueSideOccupiedPositions.clear();
        redSideOccupiedPositions.clear();
        blueSideSoftOccupiedPositions.clear();
        redSideSoftOccupiedPositions.clear();

        OccupiedPositionsResult blueResult = calculateOccupiedPositions(blueSidePicks);
        OccupiedPositionsResult redResult = calculateOccupiedPositions(redSidePicks);

        blueSideOccupiedPositions.addAll(blueResult.hard());
        redSideOccupiedPositions.addAll(redResult.hard());
        blueSideSoftOccupiedPositions.addAll(blueResult.soft());
        redSideSoftOccupiedPositions.addAll(redResult.soft());
    }

    private record OccupiedPositionsResult(Set<String> hard, Set<String> soft) {}

    private OccupiedPositionsResult calculateOccupiedPositions(List<String> picks) {
        Map<String, List<String>> champRoles = new HashMap<>();
        for (String champ : picks) {
            ChampionFlexibility flex = getFlexibility(champ);
            if (flex != null) {
                List<String> roles = getRoles(flex);
                if (!roles.isEmpty()) champRoles.put(champ, roles);
            }
        }

        // Уровень 1: железобетонные роли (constraint propagation)
        Set<String> hard = new HashSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (List<String> roles : champRoles.values()) {
                List<String> freeRoles = roles.stream()
                        .filter(role -> !hard.contains(role))
                        .toList();
                if (freeRoles.size() == 1) {
                    if (hard.add(freeRoles.get(0))) changed = true;
                }
            }
        }

        // Уровень 2: флекс-роли (все свободные роли чемпионов с >1 свободной ролью)
        Set<String> soft = new HashSet<>();
        for (List<String> roles : champRoles.values()) {
            List<String> freeRoles = roles.stream()
                    .filter(role -> !hard.contains(role))
                    .toList();
            // Если у чемпиона больше 1 свободной роли — это флекс
            if (freeRoles.size() > 1) {
                soft.addAll(freeRoles);
            }
        }

        return new OccupiedPositionsResult(hard, soft);
    }

    private ChampionFlexibility getFlexibility(String champion) {
        return flexibilityCache.computeIfAbsent(champion, c -> championFlexibilityRequests.getChampionFlexibility(c));
    }

    private List<String> getRoles(ChampionFlexibility flex) {
        List<String> roles = new ArrayList<>();
        if (flex.getTop().isPresent() && flex.getTop().get() > 0) roles.add("TOP");
        if (flex.getJungle().isPresent() && flex.getJungle().get() > 0) roles.add("JUNGLE");
        if (flex.getMiddle().isPresent() && flex.getMiddle().get() > 0) roles.add("MIDDLE");
        if (flex.getBottom().isPresent() && flex.getBottom().get() > 0) roles.add("BOTTOM");
        if (flex.getUtility().isPresent() && flex.getUtility().get() > 0) roles.add("UTILITY");
        return roles;
    }

    private boolean isChampionBlockedByOccupiedPositions(String champion, Set<String> occupiedPositions) {
        if (occupiedPositions.isEmpty()) return false;
        ChampionFlexibility flex = getFlexibility(champion);
        if (flex == null) return false;
        List<String> champRoles = getRoles(flex);
        if (champRoles.isEmpty()) return false;
        return occupiedPositions.containsAll(champRoles);
    }
}