package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DraftPredict.Service;

import jakarta.annotation.PostConstruct;
import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DTO.DraftContext;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.Champion;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.ChampionFlexibility;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.ChampionPresence;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.CounterPick;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository.CounterPickRequests;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository.RankedRequests;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Bans.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Bans.Repository.BansRepository;
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
    private final BansRepository bansRepository;
    private final RankedRequests rankedRequests;
    private final CounterPickRequests counterPickRequests;


    // ================= INITIATE VARIABLES =================


    // Initiate cache, once at app startup
    private List<String> cachedDraftPresence;
    private List<String> cachedBanRates;
    private List<String> cachedWinRates;
    private List<String> cachedPickRates;
    private List<String> cachedChampionList;

    // Caches, populated while app run (safe thread)
    private final Map<String, ChampionFlexibility> flexibilityCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> counterPicksCache = new ConcurrentHashMap<>();

    // Locked lanes for every side
    private final Set<String> blueSideOccupiedPositions = ConcurrentHashMap.newKeySet();
    private final Set<String> redSideOccupiedPositions = ConcurrentHashMap.newKeySet();

    public DraftPredictService(MatchesRepository matchesRepository,
                               ParticipantsRepository participantsRepository,
                               BansRepository bansRepository,
                               RankedRequests rankedRequests,
                               CounterPickRequests counterPickRequests) {

        this.matchesRepository = matchesRepository;
        this.participantsRepository = participantsRepository;
        this.bansRepository = bansRepository;
        this.rankedRequests = rankedRequests;
        this.counterPickRequests = counterPickRequests;
    }

    @PostConstruct
    public void initCache() {
        log.info("Loading draft predict statistics into cache...");
        cachedDraftPresence = rankedRequests.getChampionDraftPresence("%")
                .stream().map(ChampionPresence::getChampion).toList();
        cachedBanRates = bansRepository.getMostBannedChampions("%")
                .stream().map(MostBannedChampions::getChampion).toList();
        cachedWinRates = participantsRepository.getTopPerformingChampionsByWinRate("%")
                .stream().map(TopPerformingChampions::getChampion).toList();
        cachedPickRates = participantsRepository.getTopPerformingChampionsByPickRate("%")
                .stream().map(TopPerformingChampions::getChampion).toList();
        cachedChampionList = participantsRepository.getChampionList()
                .stream().map(Champion::getChampion).toList();
        log.info("""
                        Draft predict cache loaded successfully.
                        Draft presence: {},
                        Ban rates: {},
                        Win rates: {},
                        Pick rates: {},
                        Champion list: {}
                        """,
                cachedDraftPresence.size(), cachedBanRates.size(), cachedWinRates.size(), cachedPickRates.size(), cachedChampionList.size());
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
        Map<String, Integer> generalFreq = getGeneralFrequencyMap(excludedChampions);
        return new DraftContext(excludedChampions, generalFreq);
    }


    // ================= EARLY PHASE DRAFT =================


    public List<String> getBanRecommendationsEarlyPhaseDraft(List<String> blueSideBans,
                                                             List<String> redSideBans,
                                                             List<String> blueSidePicks,
                                                             List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        Map<String, Integer> freq = new HashMap<>(context.generalFreq());


        Set<String> allOccupiedPositions = new HashSet<>(blueSideOccupiedPositions);
        allOccupiedPositions.addAll(redSideOccupiedPositions);
        freq.entrySet().removeIf(entry -> isChampionBlockedByOccupiedPositions(entry.getKey(), allOccupiedPositions));

        return freq.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> getBlueSidePickRecommendationsEarlyPhaseDraft(List<String> blueSideBans,
                                                                      List<String> redSideBans,
                                                                      List<String> blueSidePicks,
                                                                      List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        Map<String, Integer> freq = new HashMap<>(context.generalFreq());

        List<String> counterPicksForBlueSide = getCounterPickListForBans(blueSideBans);
        for (String counterPickChamp : counterPicksForBlueSide) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
            }
        }

        List<String> counterPicksForBlueSideOppositePicks = getCounterPickListForOppositePicks(redSidePicks);
        for (String counterPickChamp : counterPicksForBlueSideOppositePicks) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
            }
        }


        freq.entrySet().removeIf(entry -> isChampionBlockedByOccupiedPositions(entry.getKey(), blueSideOccupiedPositions));

        return freq.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> getRedSidePickRecommendationsEarlyPhaseDraft(List<String> blueSideBans,
                                                                     List<String> redSideBans,
                                                                     List<String> blueSidePicks,
                                                                     List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        Map<String, Integer> freq = new HashMap<>(context.generalFreq());

        List<String> counterPicksForRedSide = getCounterPickListForBans(redSideBans);
        for (String counterPickChamp : counterPicksForRedSide) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
            }
        }

        List<String> counterPicksForRedSideOppositePicks = getCounterPickListForOppositePicks(blueSidePicks);
        for (String counterPickChamp : counterPicksForRedSideOppositePicks) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
            }
        }

        freq.entrySet().removeIf(entry -> isChampionBlockedByOccupiedPositions(entry.getKey(), redSideOccupiedPositions));

        return freq.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }


    // ================= LATE PHASE DRAFT =================


    public List<String> getBanRecommendationsLatePhaseDraft(List<String> blueSideBans,
                                                            List<String> redSideBans,
                                                            List<String> blueSidePicks,
                                                            List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        Map<String, Integer> freq = new HashMap<>(context.generalFreq());

        // Late phase: учитываем весь пул чемпионов
        for (String champion : cachedChampionList) {
            if (!context.excludedChampions().contains(champion)) {
                freq.merge(champion, 1, Integer::sum);
            }
        }

        Set<String> allOccupiedPositions = new HashSet<>(blueSideOccupiedPositions);
        allOccupiedPositions.addAll(redSideOccupiedPositions);
        freq.entrySet().removeIf(entry -> isChampionBlockedByOccupiedPositions(entry.getKey(), allOccupiedPositions));

        return freq.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> getBlueSidePickRecommendationsLatePhaseDraft(List<String> blueSideBans,
                                                                     List<String> redSideBans,
                                                                     List<String> blueSidePicks,
                                                                     List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        Map<String, Integer> freq = new HashMap<>(context.generalFreq());

        List<String> counterPicksForBlueSide = getCounterPickListForBans(blueSideBans);
        for (String counterPickChamp : counterPicksForBlueSide) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
            }
        }

        List<String> counterPicksForBlueSideOppositePicks = getCounterPickListForOppositePicks(redSidePicks);
        for (String counterPickChamp : counterPicksForBlueSideOppositePicks) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
            }
        }

        // Late phase: учитываем весь пул чемпионов
        for (String champion : cachedChampionList) {
            if (!context.excludedChampions().contains(champion)) {
                freq.merge(champion, 1, Integer::sum);
            }
        }

        freq.entrySet().removeIf(entry -> isChampionBlockedByOccupiedPositions(entry.getKey(), blueSideOccupiedPositions));

        return freq.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> getRedSidePickRecommendationsLatePhaseDraft(List<String> blueSideBans,
                                                                    List<String> redSideBans,
                                                                    List<String> blueSidePicks,
                                                                    List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        Map<String, Integer> freq = new HashMap<>(context.generalFreq());

        List<String> counterPicksForRedSide = getCounterPickListForBans(redSideBans);
        for (String counterPickChamp : counterPicksForRedSide) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
            }
        }

        List<String> counterPicksForRedSideOppositePicks = getCounterPickListForOppositePicks(blueSidePicks);
        for (String counterPickChamp : counterPicksForRedSideOppositePicks) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
            }
        }

        // Late phase: учитываем весь пул чемпионов
        for (String champion : cachedChampionList) {
            if (!context.excludedChampions().contains(champion)) {
                freq.merge(champion, 1, Integer::sum);
            }
        }

        freq.entrySet().removeIf(entry -> isChampionBlockedByOccupiedPositions(entry.getKey(), redSideOccupiedPositions));

        return freq.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }


    // ================= PRIVATE METHODS =================


    private Map<String, Integer> getGeneralFrequencyMap(Set<String> excludedChampions) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        Stream.of(cachedDraftPresence, cachedBanRates, cachedWinRates, cachedPickRates)
                .flatMap(List::stream)
                .filter(champion -> !excludedChampions.contains(champion))
                .forEach(champion -> frequencyMap.merge(champion, 1, Integer::sum));
        return frequencyMap;
    }

    private List<String> getCounterPickListForBans(List<String> champions) {
        List<String> allCounterPicks = new ArrayList<>();
        for (String champion : champions) {
            ChampionFlexibility flex = getFlexibility(champion);
            if (flex == null) {
                continue;
            }
            List<String> validRoles = getRoles(flex);
            for (String role : validRoles) {
                String cacheKey = champion + ":" + role;
                List<String> counters = counterPicksCache.computeIfAbsent(cacheKey,
                        key -> counterPickRequests.getBestMatchups(champion, role, "%")
                                .stream()
                                .map(CounterPick::getChampion2)
                                .toList()
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
            if (flex == null) {
                continue;
            }
            List<String> validRoles = getRoles(flex);
            for (String role : validRoles) {
                String cacheKey = champion + ":" + role;
                List<String> counters = counterPicksCache.computeIfAbsent(cacheKey,
                        key -> counterPickRequests.getWorstMatchups(champion, role, "%")
                                .stream()
                                .map(CounterPick::getChampion2)
                                .toList()
                );
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
                if (!roles.isEmpty()) {
                    champRoles.put(champ, roles);
                }
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
                    if (occupied.add(forcedRole)) {
                        changed = true;
                    }
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
        if (occupiedPositions.isEmpty()) {
            return false;
        }
        ChampionFlexibility flex = getFlexibility(champion);
        if (flex == null) {
            return false;
        }
        List<String> champRoles = getRoles(flex);
        if (champRoles.isEmpty()) {
            return false;
        }
        return occupiedPositions.containsAll(champRoles);
    }
}