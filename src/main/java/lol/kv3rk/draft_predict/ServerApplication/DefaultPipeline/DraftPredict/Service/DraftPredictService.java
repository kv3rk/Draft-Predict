package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DraftPredict.Service;

import jakarta.annotation.PostConstruct;
import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DTO.DraftContext;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.*;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository.*;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Bans.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Matches.Repository.MatchesRepository;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Participants.DTO.TopPerformingChampions;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Participants.Repository.ParticipantsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class DraftPredictService {

    private final MatchesRepository matchesRepository;
    private final ParticipantsRepository participantsRepository;
    private final RankedRequests rankedRequests;
    private final CounterPickRequests counterPickRequests;
    private final WinRateRequests winRateRequests;
    private final PickRateRequests pickRateRequests;
    private final BanRateRequests banRateRequests;
    private final DraftPresenceRequests draftPresenceRequests;
    private final BestDuoRequests bestDuoRequests;
    private final BestTrioRequests bestTrioRequests;

    private String currentPatchFilter = "%";
    private String currentPatch = "";

    private List<String> cachedDraftPresence;
    private List<String> cachedBanRates;
    private List<String> cachedWinRates;
    private List<String> cachedPickRates;
    private List<String> cachedBanRatesByActualPatch;
    private List<String> cachedDraftPresenceByActualPatch;
    private List<String> cachedWinRatesByActualPatch;
    private List<String> cachedPickRatesByActualPatch;
    private List<String> cachedChampionList;

    private final Map<String, ChampionFlexibility> flexibilityCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> counterPicksCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> bestDuoCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> bestTrioCache = new ConcurrentHashMap<>();

    private final Set<String> blueSideOccupiedPositions = ConcurrentHashMap.newKeySet();
    private final Set<String> redSideOccupiedPositions = ConcurrentHashMap.newKeySet();

    public DraftPredictService(MatchesRepository matchesRepository,
                               ParticipantsRepository participantsRepository,
                               RankedRequests rankedRequests,
                               CounterPickRequests counterPickRequests,
                               WinRateRequests winRateRequests,
                               PickRateRequests pickRateRequests,
                               BanRateRequests banRateRequests,
                               DraftPresenceRequests draftPresenceRequests,
                               BestDuoRequests bestDuoRequests,
                               BestTrioRequests bestTrioRequests) {
        this.matchesRepository = matchesRepository;
        this.participantsRepository = participantsRepository;
        this.rankedRequests = rankedRequests;
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

    @PostConstruct
    public void initCache() {
        log.info("Loading champion list into cache");
        cachedChampionList = participantsRepository.getChampionList()
                .stream().map(Champion::getChampion).toList();
        log.info("Loaded champion list into cache");
    }

    private void rebuildCache() {
        log.info("Rebuilding draft predict statistics cache for patch filter: {}", currentPatchFilter);

        cachedDraftPresence = draftPresenceRequests.getChampionDraftPresence(currentPatchFilter)
                .stream().map(ChampionPresence::getChampion).toList();
        cachedBanRates = banRateRequests.getMostBannedChampions(currentPatchFilter)
                .stream().map(MostBannedChampions::getChampion).toList();
        cachedWinRates = winRateRequests.getTopPerformingChampionsByWinRate(currentPatchFilter)
                .stream().map(TopPerformingChampions::getChampion).toList();
        cachedPickRates = pickRateRequests.getTopPerformingChampionsByPickRate(currentPatchFilter)
                .stream().map(TopPerformingChampions::getChampion).toList();

        cachedBanRatesByActualPatch = banRateRequests.getMostBannedChampionsByActualPatch(currentPatch)
                .stream().map(MostBannedChampions::getChampion).toList();
        cachedDraftPresenceByActualPatch = draftPresenceRequests.getChampionDraftPresenceByActualPatch(currentPatch)
                .stream().map(ChampionPresence::getChampion).toList();
        cachedPickRatesByActualPatch = pickRateRequests.getTopPerformingChampionsByPickRateByActualPatch(currentPatch)
                .stream().map(TopPerformingChampions::getChampion).toList();
        cachedWinRatesByActualPatch = winRateRequests.getTopPerformingChampionsByWinRateByActualPatch(currentPatch)
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
        Map<String, Integer> freq = new HashMap<>();

        List<String> bestDuos = getBestDuoList(redSidePicks);
        for (String c : bestDuos) {
            if (!context.excludedChampions().contains(c)) freq.merge(c, 1, Integer::sum);
        }
        List<String> bestTrios = getBestTrioList(redSidePicks);
        for (String c : bestTrios) {
            if (!context.excludedChampions().contains(c)) freq.merge(c, 1, Integer::sum);
        }

        int totalBans = blueSideBans.size() + redSideBans.size();
        Map<String, Integer> phaseFreq = Map.of();
        if (totalBans < 6) {
            phaseFreq = getEarlyPhaseBanRecommendations(context.excludedChampions(), redSideBans);
        }

        for (Map.Entry<String, Integer> entry : phaseFreq.entrySet()) {
            freq.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        Set<String> allOccupied = new HashSet<>(blueSideOccupiedPositions);
        allOccupied.addAll(redSideOccupiedPositions);
        freq.entrySet().removeIf(entry -> isChampionBlockedByOccupiedPositions(entry.getKey(), allOccupied));

        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> getRedSideBanRecommendations(List<String> blueSideBans,
                                                     List<String> redSideBans,
                                                     List<String> blueSidePicks,
                                                     List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        Map<String, Integer> freq = new HashMap<>();

        List<String> bestDuos = getBestDuoList(blueSidePicks);
        for (String c : bestDuos) {
            if (!context.excludedChampions().contains(c)) freq.merge(c, 1, Integer::sum);
        }
        List<String> bestTrios = getBestTrioList(blueSidePicks);
        for (String c : bestTrios) {
            if (!context.excludedChampions().contains(c)) freq.merge(c, 1, Integer::sum);
        }

        int totalBans = blueSideBans.size() + redSideBans.size();
        Map<String, Integer> phaseFreq = Map.of();
        if (totalBans < 6) {
            phaseFreq = getEarlyPhaseBanRecommendations(context.excludedChampions(), blueSideBans);
        }

        for (Map.Entry<String, Integer> entry : phaseFreq.entrySet()) {
            freq.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        Set<String> allOccupied = new HashSet<>(blueSideOccupiedPositions);
        allOccupied.addAll(redSideOccupiedPositions);
        freq.entrySet().removeIf(entry -> isChampionBlockedByOccupiedPositions(entry.getKey(), allOccupied));

        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> getBlueSidePickRecommendations(List<String> blueSideBans,
                                                       List<String> redSideBans,
                                                       List<String> blueSidePicks,
                                                       List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
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

        freq.entrySet().removeIf(entry -> isChampionBlockedByOccupiedPositions(entry.getKey(), blueSideOccupiedPositions));

        return freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> getRedSidePickRecommendations(List<String> blueSideBans,
                                                      List<String> redSideBans,
                                                      List<String> blueSidePicks,
                                                      List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
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

        freq.entrySet().removeIf(entry -> isChampionBlockedByOccupiedPositions(entry.getKey(), redSideOccupiedPositions));

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

        // Counter picks for opposite team bans (red side bans)
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
        blueSideOccupiedPositions.addAll(calculateOccupiedPositions(blueSidePicks));
        redSideOccupiedPositions.addAll(calculateOccupiedPositions(redSidePicks));
    }

    private Set<String> calculateOccupiedPositions(List<String> picks) {
        Map<String, List<String>> champRoles = new HashMap<>();
        for (String champ : picks) {
            ChampionFlexibility flex = getFlexibility(champ);
            if (flex != null) {
                List<String> roles = getRoles(flex);
                if (!roles.isEmpty()) champRoles.put(champ, roles);
            }
        }
        Set<String> occupied = new HashSet<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (List<String> roles : champRoles.values()) {
                List<String> freeRoles = roles.stream()
                        .filter(role -> !occupied.contains(role))
                        .toList();
                if (freeRoles.size() == 1) {
                    String forcedRole = freeRoles.get(0);
                    if (occupied.add(forcedRole)) changed = true;
                }
            }
        }
        return occupied;
    }

    private ChampionFlexibility getFlexibility(String champion) {
        return flexibilityCache.computeIfAbsent(champion, c -> rankedRequests.getChampionFlexibility(c));
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