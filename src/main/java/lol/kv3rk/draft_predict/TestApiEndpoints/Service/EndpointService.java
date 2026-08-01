package lol.kv3rk.draft_predict.TestApiEndpoints.Service;

import lol.kv3rk.draft_predict.DefaultPipeline.Service.GatherMatchIDs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EndpointService {

    private final GatherMatchIDs gatherMatchIDs;

    @Autowired
    public EndpointService(
            GatherMatchIDs gatherMatchIDs) {

        this.gatherMatchIDs = gatherMatchIDs;
    }

    public void go() throws InterruptedException {

        gatherMatchIDs.getMatchIDs();

    }

}
