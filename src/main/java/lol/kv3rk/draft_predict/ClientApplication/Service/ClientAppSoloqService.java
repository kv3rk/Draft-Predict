package lol.kv3rk.draft_predict.ClientApplication.Service;

import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository.BestDuoRequests;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository.CounterPickRequests;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Bans.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Participants.DTO.TopPerformingChampions;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.*;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository.RankedRequests;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Bans.Repository.BansRepository;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Matches.Repository.MatchesRepository;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Participants.Repository.ParticipantsRepository;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.Service.RankedSoloQService;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotRequestParameters;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotServerName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ClientAppSoloqService {

    private final MatchesRepository matchesRepository;
    private final ParticipantsRepository participantsRepository;
    private final BansRepository bansRepository;
    private final RiotRequestParameters riotRequestParameters;
    private final RankedRequests rankedRequests;
    private final CounterPickRequests counterPickRequests;
    private final RankedSoloQService rankedSoloQService;
    private final BestDuoRequests bestDuoRequests;

    public ClientAppSoloqService(MatchesRepository matchesRepository,
                                 ParticipantsRepository participantsRepository,
                                 BansRepository bansRepository,
                                 RiotRequestParameters riotRequestParameters,
                                 RankedRequests rankedRequests,
                                 CounterPickRequests counterPickRequests,
                                 RankedSoloQService rankedSoloQService,
                                 BestDuoRequests bestDuoRequests) {
        this.matchesRepository = matchesRepository;
        this.participantsRepository = participantsRepository;
        this.bansRepository = bansRepository;
        this.riotRequestParameters = riotRequestParameters;
        this.rankedRequests = rankedRequests;
        this.counterPickRequests = counterPickRequests;
        this.rankedSoloQService = rankedSoloQService;
        this.bestDuoRequests = bestDuoRequests;
    }

    public long countMatches() {
        long actualAmountMatches = matchesRepository.countMatches().orElse(0L);
        return actualAmountMatches;
    }

    public String actualPatch() {
        String actualPatch = matchesRepository.actualPatch().orElse("0.0");
        return actualPatch;
    }

    public List<TopPerformingChampions> getTopPerformingChampions(String orderParameter,
                                                                  String patch) {
        if (orderParameter.equals("pick_rate")) {
            if (patch.equals("All patches")) {
                return participantsRepository.getTopPerformingChampionsByPickRate("%");
            } else {
                return participantsRepository.getTopPerformingChampionsByPickRate(patch);
            }
        } else if (orderParameter.equals("win_rate")) {
            if (patch.equals("All patches")) {
                return participantsRepository.getTopPerformingChampionsByWinRate("%");
            } else {
                return participantsRepository.getTopPerformingChampionsByWinRate(patch);
            }
        }
        return List.of();
    }

    public List<MostBannedChampions> getMostBannedChampions(String patch) {
        if (patch.equals("All patches")) {
            return bansRepository.getMostBannedChampions("%");
        } else {
            return bansRepository.getMostBannedChampions(patch);
        }
    }

    public List<String> getTierParameters() {
        return riotRequestParameters.tierParameters();
    }

    public List<String> getRiotServerName() {
        List<String> riotServerName = Arrays.stream(RiotServerName.values())
                .map(Enum::name)
                .toList();
        return riotServerName;
    }

    public String lastTimeUpdate() {
        String lastTimeUpdate = matchesRepository.getDateOfLastMatch().map(
                LocalDate::toString
        ).orElse("none");
        return lastTimeUpdate;
    }

    public List<BestDuo> getBestDuoChampions(String role1, String role2,
                                             String patch, String champion) {
        if (patch.equals("All patches")) {
            return bestDuoRequests.getTop10DuoChampions(role1, role2, "%", champion);
        } else {
            return bestDuoRequests.getTop10DuoChampions(role1, role2, patch, champion);
        }
    }

    public List<BestTrio> getBestTrioChampions(String role1,
                                               String role2,
                                               String role3,
                                               String patch,
                                               String champion1,
                                               String champion2) {
        if (patch.equals("All patches")) {
            return rankedRequests.getTop10TrioChampions(role1, role2, role3, "%",
                    champion1, champion2);
        } else {
            return rankedRequests.getTop10TrioChampions(role1, role2, role3, patch,
                    champion1, champion2);
        }
    }

    public ChampionFlexibility getChampionFlexibility(String name) {
        return rankedRequests.getChampionFlexibility(name);
    }

    public List<Champion> getChampionList() {
        return participantsRepository.getChampionList();
    }

    public List<ChampionPresence> getChampionDraftPresence(String patch) {
        if (patch.equals("All patches")) {
            return rankedRequests.getChampionDraftPresence("%");
        } else {
            return rankedRequests.getChampionDraftPresence(patch);
        }
    }

    public List<CounterPick> getBestMatchUps(String champion1,
                                             String lane,
                                             String patch) {
        if (patch.equals("All patches")) {
            return counterPickRequests.getBestMatchups(champion1, lane, "%");
        } else {
            return counterPickRequests.getBestMatchups(champion1, lane, patch);
        }
    }

    // Get patch list via RankedSoloQService (unified pipeline)
    public List<String> getPatchList() {
        List<String> allPatches = new ArrayList<>(rankedSoloQService.getPatchList());
        allPatches.addFirst("All patches");
        return allPatches;
    }

    // Get unique seasons list via RankedSoloQService
    public String getActualSeason() {
        return rankedSoloQService.getSeasonList().getLast();
    }
}