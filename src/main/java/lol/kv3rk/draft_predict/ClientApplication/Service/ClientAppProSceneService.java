package lol.kv3rk.draft_predict.ClientApplication.Service;

import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneDbRequests.SystemProSceneRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ClientAppProSceneService {

    private final SystemProSceneRequests systemProSceneRequests;

    public ClientAppProSceneService(SystemProSceneRequests systemProSceneRequests) {
        this.systemProSceneRequests = systemProSceneRequests;
    }

    public Integer getAmountOfMatches() {

        Optional<Integer> amountMatches = systemProSceneRequests.getAmountOfMatches();

        return amountMatches.orElse(0);

    }
}
