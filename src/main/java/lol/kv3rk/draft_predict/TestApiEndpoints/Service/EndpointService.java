package lol.kv3rk.draft_predict.TestApiEndpoints.Service;

import lol.kv3rk.draft_predict.DefaultPipeline.Service.GatherPUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EndpointService {

    private final GatherPUUID gatherPUUID;

    @Autowired
    public EndpointService(
            GatherPUUID gatherPUUID
    ) {
        this.gatherPUUID = gatherPUUID;
    }

    public void definePUUID() {

        gatherPUUID.getSetOfEUWPlayers();
//        gatherPUUID.getSetOfNAPlayers();
//        gatherPUUID.getSetOfKRPlayers();
//        gatherPUUID.getSetOfEUNEPlayers();

    }


}
