package lol.kv3rk.draft_predict.ClientApplication.Service;

import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Bans.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Participants.DTO.TopPerformingChampions;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.*;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository.RankedRequests;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Bans.Repository.BansRepository;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Matches.Repository.MatchesRepository;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Participants.Repository.ParticipantsRepository;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotRequestParameters;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotServerName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public ClientAppSoloqService(MatchesRepository matchesRepository,
                                 ParticipantsRepository participantsRepository,
                                 BansRepository bansRepository,
                                 RiotRequestParameters riotRequestParameters,
                                 RankedRequests rankedRequests) {

        this.matchesRepository = matchesRepository;
        this.participantsRepository = participantsRepository;
        this.bansRepository = bansRepository;
        this.riotRequestParameters = riotRequestParameters;
        this.rankedRequests = rankedRequests;
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

            return rankedRequests.getBestDuoChampions(role1, role2, "%", champion);
        } else {

            return rankedRequests.getBestDuoChampions(role1, role2, patch, champion);
        }
    }

    public List<BestTrio> getBestTrioChampions(String role1,
                                               String role2,
                                               String role3,
                                               String patch,
                                               String champion1,
                                               String champion2) {
        if (patch.equals("All patches")) {

            return rankedRequests.getBestTrioChampions(role1, role2, role3, "%",
                    champion1, champion2);
        } else {

            return rankedRequests.getBestTrioChampions(role1, role2, role3, patch,
                    champion1, champion2);
        }
    }

    public ChampionFlexibility getChampionFlexibility(String name, String patch) {
        if (patch.equals("All patches")) {
            return rankedRequests.getChampionFlexibility(name, "%");
        } else {
            return rankedRequests.getChampionFlexibility(name, patch);
        }
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

    public List<CounterPick> getCounterPick(String champion1,
                                            String lane,
                                            String patch) {
        if (patch.equals("All patches")) {

            return rankedRequests.getCounterPicks(champion1, lane, "%");
        } else {

            return rankedRequests.getCounterPicks(champion1, lane, patch);
        }

    }

    public List<String> getPatchList() {

        List<String> allPatches = matchesRepository.getPatchList();

        allPatches.addFirst("All patches");

        return allPatches;
    }
}
