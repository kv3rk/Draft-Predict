package lol.kv3rk.draft_predict.ClientApplication.Service;

import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.DTO.*;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.Repository.*;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.Service.SoloQDbRequestsService;
import lol.kv3rk.draft_predict.common.RiotParameters.RiotRequestParameters;
import lol.kv3rk.draft_predict.common.RiotParameters.RiotServerName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ClientAppSoloqService {

    private final RiotRequestParameters riotRequestParameters;
    private final ChampionFlexibilityRequests championFlexibilityRequests;
    private final CounterPickRequests counterPickRequests;
    private final BestDuoRequests bestDuoRequests;
    private final BestTrioRequests bestTrioRequests;
    private final DraftPresenceRequests draftPresenceRequests;
    private final BanRateRequests banRateRequests;
    private final SystemRankedRequests systemRankedRequests;
    private final WinRateRequests winRateRequests;
    private final PickRateRequests pickRateRequests;
    private final SoloQDbRequestsService soloQDbRequestsService;

    public ClientAppSoloqService(RiotRequestParameters riotRequestParameters,
                                 ChampionFlexibilityRequests championFlexibilityRequests,
                                 CounterPickRequests counterPickRequests,
                                 BestDuoRequests bestDuoRequests,
                                 BestTrioRequests bestTrioRequests,
                                 DraftPresenceRequests draftPresenceRequests,
                                 BanRateRequests banRateRequests,
                                 SystemRankedRequests systemRankedRequests,
                                 WinRateRequests winRateRequests,
                                 PickRateRequests pickRateRequests,
                                 SoloQDbRequestsService soloQDbRequestsService) {

        this.riotRequestParameters = riotRequestParameters;
        this.championFlexibilityRequests = championFlexibilityRequests;
        this.counterPickRequests = counterPickRequests;
        this.bestDuoRequests = bestDuoRequests;
        this.bestTrioRequests = bestTrioRequests;
        this.draftPresenceRequests = draftPresenceRequests;
        this.banRateRequests = banRateRequests;
        this.systemRankedRequests = systemRankedRequests;
        this.winRateRequests = winRateRequests;
        this.pickRateRequests = pickRateRequests;
        this.soloQDbRequestsService = soloQDbRequestsService;
    }

    public long countMatches() {
        long actualAmountMatches = systemRankedRequests.countMatches().orElse(0L);
        return actualAmountMatches;
    }

    public String actualPatch() {
        String actualPatch = systemRankedRequests.actualPatch().orElse("0.0");
        return actualPatch;
    }

    public List<TopPerformingChampions> getTopPerformingChampions(String orderParameter,
                                                                  String patch) {
        if (orderParameter.equals("pick_rate")) {
            if (patch.equals("All patches")) {
                return pickRateRequests.getTop10PickRate("%");
            } else {
                return pickRateRequests.getTop10PickRate(patch);
            }
        } else if (orderParameter.equals("win_rate")) {
            if (patch.equals("All patches")) {
                return winRateRequests.getTop10WinRate("%");
            } else {
                return winRateRequests.getTop10WinRate(patch);
            }
        }
        return List.of();
    }

    public List<MostBannedChampions> getMostBannedChampions(String patch) {
        if (patch.equals("All patches")) {
            return banRateRequests.getTop10BannedChampions("%");
        } else {
            return banRateRequests.getTop10BannedChampions(patch);
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
        String lastTimeUpdate = systemRankedRequests.getDateOfLastMatch().map(
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
            return bestTrioRequests.getTop10TrioChampions(role1, role2, role3, "%",
                    champion1, champion2);
        } else {
            return bestTrioRequests.getTop10TrioChampions(role1, role2, role3, patch,
                    champion1, champion2);
        }
    }

    public ChampionFlexibility getChampionFlexibility(String name) {
        return championFlexibilityRequests.getChampionFlexibility(name);
    }

    public List<Champion> getChampionList() {
        return systemRankedRequests.getChampionList();
    }

    public List<ChampionPresence> getChampionDraftPresence(String patch) {
        if (patch.equals("All patches")) {
            return draftPresenceRequests.getTop10ChampionDraftPresence("%");
        } else {
            return draftPresenceRequests.getTop10ChampionDraftPresence(patch);
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
        List<String> allPatches = new ArrayList<>(systemRankedRequests.getPatchList());
        allPatches.addFirst("All patches");
        return allPatches;
    }

    // Get unique seasons list via RankedSoloQService
    public Integer getActualSeason() {
        return soloQDbRequestsService.getSeasonList().getLast();
    }
}