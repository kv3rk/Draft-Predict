package lol.kv3rk.draft_predict.ClientApplication.Service;

import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DraftPredict.Service.DraftPredictService;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.Champion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ClientAppDraftService {

    private final ClientAppSoloqService clientAppSoloqService;
    private final DraftPredictService draftPredictService;

    public ClientAppDraftService(ClientAppSoloqService clientAppSoloqService,
                                 DraftPredictService draftPredictService) {
        this.clientAppSoloqService = clientAppSoloqService;
        this.draftPredictService = draftPredictService;
    }

    public List<Champion> getChampionList() {
        return clientAppSoloqService.getChampionList();
    }

    public List<String> getPatchList() {
        return clientAppSoloqService.getPatchList();
    }

    // --- EARLY PHASE DRAFT ---

    public List<String> getBanRecommendationsEarlyPhaseDraft(List<String> blueSideBans,
                                                             List<String> redSideBans,
                                                             List<String> blueSidePicks,
                                                             List<String> redSidePicks) {
        return draftPredictService.getBanRecommendationsEarlyPhaseDraft(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
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

    public List<String> getBanRecommendationsLatePhaseDraft(List<String> blueSideBans,
                                                            List<String> redSideBans,
                                                            List<String> blueSidePicks,
                                                            List<String> redSidePicks) {
        return draftPredictService.getBanRecommendationsLatePhaseDraft(blueSideBans, redSideBans, blueSidePicks, redSidePicks);
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