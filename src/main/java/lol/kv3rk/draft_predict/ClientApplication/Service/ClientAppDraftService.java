package lol.kv3rk.draft_predict.ClientApplication.Service;

import lol.kv3rk.draft_predict.ClientApplication.DTO.Draft.MatchSetupInfoDTO;
import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DraftPredict.Service.DraftPredictService;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.Champion;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.Service.RankedSoloQService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ClientAppDraftService {
    private final ClientAppSoloqService clientAppSoloqService;
    private final DraftPredictService draftPredictService;
    private final RankedSoloQService rankedSoloQService;

    public ClientAppDraftService(ClientAppSoloqService clientAppSoloqService,
                                 DraftPredictService draftPredictService,
                                 RankedSoloQService rankedSoloQService) {
        this.clientAppSoloqService = clientAppSoloqService;
        this.draftPredictService = draftPredictService;
        this.rankedSoloQService = rankedSoloQService;
    }

    public List<Champion> getChampionList() {
        return clientAppSoloqService.getChampionList();
    }

    public List<String> getPatchList() {
        return rankedSoloQService.getPatchList();
    }

    // Get unique seasons list via RankedSoloQService
    public List<String> getSeasonList() {
        return rankedSoloQService.getSeasonList();
    }

    // Setup match info with season and patch
    public Map<String, String> setupMatchInfo(MatchSetupInfoDTO dto) {
        log.info("Setting up match info - Season: {}, Patch: {}", dto.season(), dto.patch());
        String patchFilter = dto.season() + ".%";
        draftPredictService.setupMatchInfo(patchFilter, dto.patch());
        return Map.of(
                "status", "success",
                "season", dto.season(),
                "patch", dto.patch(),
                "patchFilter", patchFilter,
                "draftType", dto.draftType(),
                "firstPickSide", dto.firstPickSide()
        );
    }

    // ================= DRAFT RECOMMENDATIONS =================
    public List<String> getBlueSideBanRecommendations(List<String> blueSideBans,
                                                      List<String> redSideBans,
                                                      List<String> blueSidePicks,
                                                      List<String> redSidePicks) {
        return draftPredictService.getBlueSideBanRecommendations(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
    }

    public List<String> getRedSideBanRecommendations(List<String> blueSideBans,
                                                     List<String> redSideBans,
                                                     List<String> blueSidePicks,
                                                     List<String> redSidePicks) {
        return draftPredictService.getRedSideBanRecommendations(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
    }

    public List<String> getBlueSidePickRecommendations(List<String> blueSideBans,
                                                       List<String> redSideBans,
                                                       List<String> blueSidePicks,
                                                       List<String> redSidePicks) {
        return draftPredictService.getBlueSidePickRecommendations(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
    }

    public List<String> getRedSidePickRecommendations(List<String> blueSideBans,
                                                      List<String> redSideBans,
                                                      List<String> blueSidePicks,
                                                      List<String> redSidePicks) {
        return draftPredictService.getRedSidePickRecommendations(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
    }
}