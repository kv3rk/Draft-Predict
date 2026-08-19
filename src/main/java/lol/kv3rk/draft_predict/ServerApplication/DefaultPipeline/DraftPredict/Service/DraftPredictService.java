package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DraftPredict.Service;

import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.ChampionPresence;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository.RankedRequests;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Bans.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Bans.Repository.BansRepository;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Matches.Repository.MatchesRepository;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Participants.DTO.TopPerformingChampions;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Participants.Repository.ParticipantsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class DraftPredictService {
    private final MatchesRepository matchesRepository;
    private final ParticipantsRepository participantsRepository;
    private final BansRepository bansRepository;
    private final RankedRequests rankedRequests;

    public DraftPredictService(MatchesRepository matchesRepository,
                               ParticipantsRepository participantsRepository,
                               BansRepository bansRepository,
                               RankedRequests rankedRequests) {

        this.matchesRepository = matchesRepository;
        this.participantsRepository = participantsRepository;
        this.bansRepository = bansRepository;
        this.rankedRequests = rankedRequests;
    }

    public List<String> getBanRecommendations(List<String> blueSideBans,
                                              List<String> redSideBans,
                                              List<String> blueSidePicks,
                                              List<String> redSidePicks) {

        Set<String> excludedChampions = Stream.of(
                blueSideBans.stream(),
                redSideBans.stream(),
                blueSidePicks.stream(),
                redSidePicks.stream()
        ).flatMap(s -> s).collect(Collectors.toSet());

        Map<String, Integer> generalFreq = getGeneralFrequencyMap(excludedChampions);

        return getTop3ChampionsForBans(excludedChampions, generalFreq);
    }


    public List<String> getBlueSidePickRecommendations(List<String> blueSideBans,
                                                       List<String> redSideBans,
                                                       List<String> blueSidePicks,
                                                       List<String> redSidePicks) {

        Set<String> excludedChampions = Stream.of(
                blueSideBans.stream(),
                redSideBans.stream(),
                blueSidePicks.stream(),
                redSidePicks.stream()
        ).flatMap(s -> s).collect(Collectors.toSet());

        Map<String, Integer> generalFreq = getGeneralFrequencyMap(excludedChampions);

        return getTop3ChampionsForBlueSidePicks(excludedChampions, generalFreq);
    }


    public List<String> getRedSidePickRecommendations(List<String> blueSideBans,
                                                      List<String> redSideBans,
                                                      List<String> blueSidePicks,
                                                      List<String> redSidePicks) {

        Set<String> excludedChampions = Stream.of(
                blueSideBans.stream(),
                redSideBans.stream(),
                blueSidePicks.stream(),
                redSidePicks.stream()
        ).flatMap(s -> s).collect(Collectors.toSet());

        Map<String, Integer> generalFreq = getGeneralFrequencyMap(excludedChampions);

        return getTop3ChampionsForRedSidePicks(excludedChampions, generalFreq);
    }


    private List<String> getTop3ChampionsForBans(Set<String> excludedChampions,
                                                 Map<String, Integer> generalFrequencyMap) {

        Map<String, Integer> freq = new HashMap<>(generalFrequencyMap);

        // Индивидуальная логика для банов (если нужна)
        // Например: усилить вес банов
        bansRepository.getMostBannedChampions("%")
                .forEach(b -> freq.merge(b.getChampion(), 1, Integer::sum));

        return freq.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<String> getTop3ChampionsForBlueSidePicks(Set<String> excludedChampions,
                                                          Map<String, Integer> generalFrequencyMap) {

        Map<String, Integer> freq = new HashMap<>(generalFrequencyMap);

        // Индивидуальная логика для Blue Side picks
        // Например: усилить вес win rate
        participantsRepository.getTopPerformingChampionsByWinRate("%")
                .forEach(c -> freq.merge(c.getChampion(), 1, Integer::sum));

        return freq.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }

    private List<String> getTop3ChampionsForRedSidePicks(Set<String> excludedChampions,
                                                         Map<String, Integer> generalFrequencyMap) {

        Map<String, Integer> freq = new HashMap<>(generalFrequencyMap);

        // Индивидуальная логика для Red Side picks
        // Например: усилить вес pick rate
        participantsRepository.getTopPerformingChampionsByPickRate("%")
                .forEach(c -> freq.merge(c.getChampion(), 1, Integer::sum));

        return freq.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }


    private Map<String, Integer> getGeneralFrequencyMap(Set<String> excludedChampions) {

        List<String> draftPresence = rankedRequests.getChampionDraftPresence("%")
                .stream()
                .map(ChampionPresence::getChampion)
                .toList();

        List<String> banRates = bansRepository.getMostBannedChampions("%")
                .stream()
                .map(MostBannedChampions::getChampion)
                .toList();

        List<String> winRates = participantsRepository.getTopPerformingChampionsByWinRate("%")
                .stream()
                .map(TopPerformingChampions::getChampion)
                .toList();

        List<String> pickRates = participantsRepository.getTopPerformingChampionsByPickRate("%")
                .stream()
                .map(TopPerformingChampions::getChampion)
                .toList();

        Map<String, Integer> frequencyMap = new HashMap<>();

        Stream.of(draftPresence, banRates, winRates, pickRates)
                .flatMap(List::stream)
                .filter(champion -> !excludedChampions.contains(champion))
                .forEach(champion -> frequencyMap.merge(champion, 1, Integer::sum));

        return frequencyMap;
    }

}