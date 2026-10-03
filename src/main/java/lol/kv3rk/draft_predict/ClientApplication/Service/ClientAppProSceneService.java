package lol.kv3rk.draft_predict.ClientApplication.Service;

import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneDbRequests.DTO.ProChampion;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneDbRequests.Repository.ProPickRateRequests;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneDbRequests.Repository.SystemProSceneRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ClientAppProSceneService {

    private final SystemProSceneRequests systemProSceneRequests;
    private final ProPickRateRequests proPickRateRequests;

    public ClientAppProSceneService(SystemProSceneRequests systemProSceneRequests,
                                    ProPickRateRequests proPickRateRequests) {
        this.systemProSceneRequests = systemProSceneRequests;
        this.proPickRateRequests = proPickRateRequests;
    }

    public Long getAmountOfMatches() {

        Optional<Long> amountMatches = systemProSceneRequests.getAmountOfMatches();

        return amountMatches.orElse(0L);

    }

    public List<ProChampion> getProChampionPickRate() {

        return proPickRateRequests.getProChampionPickRate();
    }
}
