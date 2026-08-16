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
                                              List<String> redSideBans) {

        Set<String> excludedChampions = Stream.concat(blueSideBans.stream(), redSideBans.stream())
                .collect(Collectors.toSet());

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

        for (String champion : draftPresence) {
            if (!excludedChampions.contains(champion)) {
                frequencyMap.merge(champion, 1, Integer::sum);
            }
        }
        for (String champion : banRates) {
            if (!excludedChampions.contains(champion)) {
                frequencyMap.merge(champion, 1, Integer::sum);
            }
        }
        for (String champion : winRates) {
            if (!excludedChampions.contains(champion)) {
                frequencyMap.merge(champion, 1, Integer::sum);
            }
        }
        for (String champion : pickRates) {
            if (!excludedChampions.contains(champion)) {
                frequencyMap.merge(champion, 1, Integer::sum);
            }
        }

        return frequencyMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> redSideFirstPick() {
        return List.of();
    }

    public List<String> blueSideFirstPick() {
        return List.of();
    }

    public List<String> redSideSecondPick() {
        return List.of();
    }

    public List<String> blueSideSecondPick() {
        return List.of();
    }
}