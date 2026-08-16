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

    public List<String> getBanRecommendations(List<String> blueSideBans,
                                              List<String> redSideBans) {

        return draftPredictService.getBanRecommendations(blueSideBans, redSideBans);
    }
}
