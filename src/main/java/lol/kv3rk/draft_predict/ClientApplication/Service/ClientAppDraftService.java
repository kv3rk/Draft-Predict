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

        // Build patch filter: season + ".%" for all patches in that season
        String patchFilter = dto.season() + ".%";

        // Pass to DraftPredictService
        draftPredictService.setupMatchInfo(patchFilter, dto.patch());

        // Return confirmation (all fields included, no mutation needed)
        return Map.of(
                "status", "success",
                "season", dto.season(),
                "patch", dto.patch(),
                "patchFilter", patchFilter,
                "firstPickSide", dto.firstPickSide()
        );
    }

    // --- EARLY PHASE DRAFT ---
    public List<String> getBlueSideBanRecommendationsEarlyPhaseDraft(List<String> blueSideBans,
                                                                     List<String> redSideBans,
                                                                     List<String> blueSidePicks,
                                                                     List<String> redSidePicks) {
        return draftPredictService.getBlueSideBanRecommendationsEarlyPhaseDraft(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
    }

    public List<String> getRedSideBanRecommendationsEarlyPhaseDraft(List<String> blueSideBans,
                                                                    List<String> redSideBans,
                                                                    List<String> blueSidePicks,
                                                                    List<String> redSidePicks) {
        return draftPredictService.getRedSideBanRecommendationsEarlyPhaseDraft(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
    }

    public List<String> getBlueSidePickRecommendationsEarlyPhaseDraft(List<String> blueSideBans,
                                                                      List<String> redSideBans,
                                                                      List<String> blueSidePicks,
                                                                      List<String> redSidePicks) {
        return draftPredictService.getBlueSidePickRecommendationsEarlyPhaseDraft(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
    }

    public List<String> getRedSidePickRecommendationsEarlyPhaseDraft(List<String> blueSideBans,
                                                                     List<String> redSideBans,
                                                                     List<String> blueSidePicks,
                                                                     List<String> redSidePicks) {
        return draftPredictService.getRedSidePickRecommendationsEarlyPhaseDraft(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
    }

    // --- LATE PHASE DRAFT ---
    public List<String> getBlueSideBanRecommendationsLatePhaseDraft(List<String> blueSideBans,
                                                                    List<String> redSideBans,
                                                                    List<String> blueSidePicks,
                                                                    List<String> redSidePicks) {
        return draftPredictService.getBlueSideBanRecommendationsLatePhaseDraft(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
    }

    public List<String> getRedSideBanRecommendationsLatePhaseDraft(List<String> blueSideBans,
                                                                   List<String> redSideBans,
                                                                   List<String> blueSidePicks,
                                                                   List<String> redSidePicks) {
        return draftPredictService.getRedSideBanRecommendationsLatePhaseDraft(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
    }

    public List<String> getBlueSidePickRecommendationsLatePhaseDraft(List<String> blueSideBans,
                                                                     List<String> redSideBans,
                                                                     List<String> blueSidePicks,
                                                                     List<String> redSidePicks) {
        return draftPredictService.getBlueSidePickRecommendationsLatePhaseDraft(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
    }

    public List<String> getRedSidePickRecommendationsLatePhaseDraft(List<String> blueSideBans,
                                                                    List<String> redSideBans,
                                                                    List<String> blueSidePicks,
                                                                    List<String> redSidePicks) {
        return draftPredictService.getRedSidePickRecommendationsLatePhaseDraft(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
    }
}