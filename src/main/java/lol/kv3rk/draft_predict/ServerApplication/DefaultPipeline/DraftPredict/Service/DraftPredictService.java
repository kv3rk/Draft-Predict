package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DraftPredict.Service;

import jakarta.annotation.PostConstruct;
import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DTO.DraftContext;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.BestDuo;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.BestTrio;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.Champion;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.ChampionFlexibility;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.ChampionPresence;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.CounterPick;
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

    // ================= INITIATE VARIABLES =================
    // Initiate cache, once at app startup
    private List<String> cachedDraftPresenceEarlyDraftPhase;
    private List<String> cachedBanRatesEarlyDraftPhase;
    private List<String> cachedWinRatesEarlyDraftPhase;
    private List<String> cachedPickRatesEarlyDraftPhase;
    private List<String> cachedDraftPresenceLateDraftPhase;
    private List<String> cachedBanRatesLateDraftPhase;
    private List<String> cachedWinRatesLateDraftPhase;
    private List<String> cachedPickRatesLateDraftPhase;
    private List<String> cachedBanRatesByActualPatch;
    private List<String> cachedChampionList;

    // Caches, populated while app run (safe thread)
    private final Map<String, ChampionFlexibility> flexibilityCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> counterPicksCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> bestDuoCache = new ConcurrentHashMap<>();
    private final Map<String, List<String>> bestTrioCache = new ConcurrentHashMap<>();

    // Locked lanes for every side
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

    @PostConstruct
    public void initCache() {
        log.info("Loading draft predict statistics into cache");

        //Early draft phase cache
        cachedDraftPresenceEarlyDraftPhase = draftPresenceRequests.getChampionDraftPresenceEarlyDraftPhase("%")
                .stream().map(ChampionPresence::getChampion).toList();
        cachedBanRatesEarlyDraftPhase = banRateRequests.getMostBannedChampionsEarlyDraftPhase("%")
                .stream().map(MostBannedChampions::getChampion).toList();
        cachedWinRatesEarlyDraftPhase = winRateRequests.getTopPerformingChampionsByWinRateEarlyDraftPhase("%")
                .stream().map(TopPerformingChampions::getChampion).toList();
        cachedPickRatesEarlyDraftPhase = pickRateRequests.getTopPerformingChampionsByPickRateEarlyDraftPhase("%")
                .stream().map(TopPerformingChampions::getChampion).toList();

        //Late draft phase cache
        cachedDraftPresenceLateDraftPhase = draftPresenceRequests.getChampionDraftPresenceLateDraftPhase("%")
                .stream().map(ChampionPresence::getChampion).toList();
        cachedBanRatesLateDraftPhase = banRateRequests.getMostBannedChampionsLateDraftPhase("%")
                .stream().map(MostBannedChampions::getChampion).toList();
        cachedWinRatesLateDraftPhase = winRateRequests.getTopPerformingChampionsByWinRateLateDraftPhase("%")
                .stream().map(TopPerformingChampions::getChampion).toList();
        cachedPickRatesLateDraftPhase = pickRateRequests.getTopPerformingChampionsByPickRateLateDraftPhase("%")
                .stream().map(TopPerformingChampions::getChampion).toList();

        //Other cached data
        cachedChampionList = participantsRepository.getChampionList()
                .stream().map(Champion::getChampion).toList();
        cachedBanRatesByActualPatch = banRateRequests.getMostBannedChampionsByActualPatch("16.16")
                .stream().map(MostBannedChampions::getChampion).toList();

        log.info("Loaded draft predict statistics into cache");
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

    // ================= EARLY PHASE DRAFT =================

    public List<String> getBlueSideBanRecommendationsEarlyPhaseDraft(List<String> blueSideBans,
                                                                     List<String> redSideBans,
                                                                     List<String> blueSidePicks,
                                                                     List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        Map<String, Integer> freq = new HashMap<>(getBanFrequencyMapEarlyPhaseDraft(context.excludedChampions()));

        // Интеграция контр-пиков для банов противоположной команды (red side bans)
        List<String> counterPicksForRedSideBans = getCounterPickListForBans(redSideBans);
        for (String counterPickChamp : counterPicksForRedSideBans) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
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

    public List<String> getRedSideBanRecommendationsEarlyPhaseDraft(List<String> blueSideBans,
                                                                    List<String> redSideBans,
                                                                    List<String> blueSidePicks,
                                                                    List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        Map<String, Integer> freq = new HashMap<>(getBanFrequencyMapEarlyPhaseDraft(context.excludedChampions()));

        // Интеграция контр-пиков для банов противоположной команды (blue side bans)
        List<String> counterPicksForBlueSideBans = getCounterPickListForBans(blueSideBans);
        for (String counterPickChamp : counterPicksForBlueSideBans) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
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

    public List<String> getBlueSidePickRecommendationsEarlyPhaseDraft(List<String> blueSideBans,
                                                                      List<String> redSideBans,
                                                                      List<String> blueSidePicks,
                                                                      List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        Map<String, Integer> freq = new HashMap<>(getPickFrequencyMapEarlyPhaseDraft(context.excludedChampions()));

        List<String> counterPicksForBlueSideTeamBans = getCounterPickListForBans(blueSideBans);
        for (String counterPickChamp : counterPicksForBlueSideTeamBans) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
            }
        }

        List<String> counterPicksForBlueSideOppositeBans = getCounterPickListForBans(redSideBans);
        for (String counterPickChamp : counterPicksForBlueSideOppositeBans) {
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

        List<String> bestDuosForBlueSidePicks = getBestDuoList(blueSidePicks);
        for (String duoChamp : bestDuosForBlueSidePicks) {
            if (!context.excludedChampions().contains(duoChamp)) {
                freq.merge(duoChamp, 1, Integer::sum);
            }
        }

        // Интеграция Best Trio для пиков Blue Side
        List<String> bestTriosForBlueSidePicks = getBestTrioList(blueSidePicks);
        for (String trioChamp : bestTriosForBlueSidePicks) {
            if (!context.excludedChampions().contains(trioChamp)) {
                freq.merge(trioChamp, 1, Integer::sum);
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
        Map<String, Integer> freq = new HashMap<>(getPickFrequencyMapEarlyPhaseDraft(context.excludedChampions()));

        List<String> counterPicksForRedSideTeamBans = getCounterPickListForBans(redSideBans);
        for (String counterPickChamp : counterPicksForRedSideTeamBans) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
            }
        }

        List<String> counterPicksForRedSideOppositeBans = getCounterPickListForBans(blueSideBans);
        for (String counterPickChamp : counterPicksForRedSideOppositeBans) {
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

        List<String> bestDuosForRedSidePicks = getBestDuoList(redSidePicks);
        for (String duoChamp : bestDuosForRedSidePicks) {
            if (!context.excludedChampions().contains(duoChamp)) {
                freq.merge(duoChamp, 1, Integer::sum);
            }
        }

        // Интеграция Best Trio для пиков Red Side
        List<String> bestTriosForRedSidePicks = getBestTrioList(redSidePicks);
        for (String trioChamp : bestTriosForRedSidePicks) {
            if (!context.excludedChampions().contains(trioChamp)) {
                freq.merge(trioChamp, 1, Integer::sum);
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

    public List<String> getBlueSideBanRecommendationsLatePhaseDraft(List<String> blueSideBans,
                                                                    List<String> redSideBans,
                                                                    List<String> blueSidePicks,
                                                                    List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        Map<String, Integer> freq = new HashMap<>(getBanFrequencyMapLatePhaseDraft(context.excludedChampions()));

        // Интеграция Best Duo для пиков противоположной команды (red side picks)
        List<String> bestDuosForRedSidePicks = getBestDuoList(redSidePicks);
        for (String duoChamp : bestDuosForRedSidePicks) {
            if (!context.excludedChampions().contains(duoChamp)) {
                freq.merge(duoChamp, 1, Integer::sum);
            }
        }

        // Интеграция Best Trio для пиков противоположной команды (red side picks)
        List<String> bestTriosForRedSidePicks = getBestTrioList(redSidePicks);
        for (String trioChamp : bestTriosForRedSidePicks) {
            if (!context.excludedChampions().contains(trioChamp)) {
                freq.merge(trioChamp, 1, Integer::sum);
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

    public List<String> getRedSideBanRecommendationsLatePhaseDraft(List<String> blueSideBans,
                                                                   List<String> redSideBans,
                                                                   List<String> blueSidePicks,
                                                                   List<String> redSidePicks) {
        DraftContext context = prepareDraftContext(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        Map<String, Integer> freq = new HashMap<>(getBanFrequencyMapLatePhaseDraft(context.excludedChampions()));

        // Интеграция Best Duo для пиков противоположной команды (blue side picks)
        List<String> bestDuosForBlueSidePicks = getBestDuoList(blueSidePicks);
        for (String duoChamp : bestDuosForBlueSidePicks) {
            if (!context.excludedChampions().contains(duoChamp)) {
                freq.merge(duoChamp, 1, Integer::sum);
            }
        }

        // Интеграция Best Trio для пиков противоположной команды (blue side picks)
        List<String> bestTriosForBlueSidePicks = getBestTrioList(blueSidePicks);
        for (String trioChamp : bestTriosForBlueSidePicks) {
            if (!context.excludedChampions().contains(trioChamp)) {
                freq.merge(trioChamp, 1, Integer::sum);
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
        Map<String, Integer> freq = new HashMap<>(getPickFrequencyMapLatePhaseDraft(context.excludedChampions()));

        List<String> counterPicksForBlueSideTeamBans = getCounterPickListForBans(blueSideBans);
        for (String counterPickChamp : counterPicksForBlueSideTeamBans) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
            }
        }

        List<String> counterPicksForBlueSideOppositeBans = getCounterPickListForBans(redSideBans);
        for (String counterPickChamp : counterPicksForBlueSideOppositeBans) {
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

        List<String> bestDuosForBlueSidePicks = getBestDuoList(blueSidePicks);
        for (String duoChamp : bestDuosForBlueSidePicks) {
            if (!context.excludedChampions().contains(duoChamp)) {
                freq.merge(duoChamp, 1, Integer::sum);
            }
        }

        // Интеграция Best Trio для пиков Blue Side
        List<String> bestTriosForBlueSidePicks = getBestTrioList(blueSidePicks);
        for (String trioChamp : bestTriosForBlueSidePicks) {
            if (!context.excludedChampions().contains(trioChamp)) {
                freq.merge(trioChamp, 1, Integer::sum);
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
        Map<String, Integer> freq = new HashMap<>(getPickFrequencyMapLatePhaseDraft(context.excludedChampions()));

        List<String> counterPicksForRedSideTeamBans = getCounterPickListForBans(redSideBans);
        for (String counterPickChamp : counterPicksForRedSideTeamBans) {
            if (!context.excludedChampions().contains(counterPickChamp)) {
                freq.merge(counterPickChamp, 1, Integer::sum);
            }
        }

        List<String> counterPicksForRedSideOppositeBans = getCounterPickListForBans(blueSideBans);
        for (String counterPickChamp : counterPicksForRedSideOppositeBans) {
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

        List<String> bestDuosForRedSidePicks = getBestDuoList(redSidePicks);
        for (String duoChamp : bestDuosForRedSidePicks) {
            if (!context.excludedChampions().contains(duoChamp)) {
                freq.merge(duoChamp, 1, Integer::sum);
            }
        }

        // Интеграция Best Trio для пиков Red Side
        List<String> bestTriosForRedSidePicks = getBestTrioList(redSidePicks);
        for (String trioChamp : bestTriosForRedSidePicks) {
            if (!context.excludedChampions().contains(trioChamp)) {
                freq.merge(trioChamp, 1, Integer::sum);
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

    private List<String> getBestDuoList(List<String> champions) {
        List<String> allBestDuos = new ArrayList<>();
        for (String champion : champions) {
            String cacheKey = "duo:" + champion;
            List<String> duos = bestDuoCache.computeIfAbsent(cacheKey,
                    key -> bestDuoRequests.getBestDuoChampionsWithoutRoleConstraint("%", champion)
                            .stream()
                            .map(BestDuo::getChampion2)
                            .toList()
            );
            allBestDuos.addAll(duos);
        }
        return allBestDuos;
    }

    private List<String> getBestTrioList(List<String> champions) {
        List<String> allBestTrios = new ArrayList<>();
        // Формируем все уникальные пары из списка чемпионов
        for (int i = 0; i < champions.size(); i++) {
            for (int j = i + 1; j < champions.size(); j++) {
                String champ1 = champions.get(i);
                String champ2 = champions.get(j);
                // Нормализуем ключ, чтобы (A,B) и (B,A) давали один и тот же ключ кэша
                String normalizedKey = champ1.compareTo(champ2) <= 0
                        ? "trio:" + champ1 + ":" + champ2
                        : "trio:" + champ2 + ":" + champ1;
                List<String> trios = bestTrioCache.computeIfAbsent(normalizedKey,
                        key -> bestTrioRequests.getBestTrioChampionsNoRole("%", champ1, champ2)
                                .stream()
                                .map(BestTrio::getChampion3)
                                .toList()
                );
                allBestTrios.addAll(trios);
            }
        }
        return allBestTrios;
    }

    private Map<String, Integer> getBanFrequencyMapEarlyPhaseDraft(Set<String> excludedChampions) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        Stream.of(cachedDraftPresenceEarlyDraftPhase, cachedBanRatesEarlyDraftPhase, cachedChampionList, cachedBanRatesByActualPatch, cachedPickRatesEarlyDraftPhase, cachedWinRatesEarlyDraftPhase)
                .flatMap(List::stream)
                .filter(champion -> !excludedChampions.contains(champion))
                .forEach(champion -> frequencyMap.merge(champion, 1, Integer::sum));
        return frequencyMap;
    }

    private Map<String, Integer> getBanFrequencyMapLatePhaseDraft(Set<String> excludedChampions) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        Stream.of(cachedDraftPresenceLateDraftPhase, cachedBanRatesLateDraftPhase, cachedChampionList, cachedWinRatesLateDraftPhase, cachedPickRatesLateDraftPhase)
                .flatMap(List::stream)
                .filter(champion -> !excludedChampions.contains(champion))
                .forEach(champion -> frequencyMap.merge(champion, 1, Integer::sum));
        return frequencyMap;
    }

    private Map<String, Integer> getPickFrequencyMapEarlyPhaseDraft(Set<String> excludedChampions) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        Stream.of(cachedDraftPresenceEarlyDraftPhase, cachedPickRatesEarlyDraftPhase, cachedWinRatesEarlyDraftPhase, cachedChampionList, cachedBanRatesEarlyDraftPhase)
                .flatMap(List::stream)
                .filter(champion -> !excludedChampions.contains(champion))
                .forEach(champion -> frequencyMap.merge(champion, 1, Integer::sum));
        return frequencyMap;
    }

    private Map<String, Integer> getPickFrequencyMapLatePhaseDraft(Set<String> excludedChampions) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        Stream.of(cachedDraftPresenceLateDraftPhase, cachedPickRatesLateDraftPhase, cachedWinRatesLateDraftPhase, cachedChampionList, cachedBanRatesLateDraftPhase)
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