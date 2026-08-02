package lol.kv3rk.draft_predict.TestApiEndpoints.Service;

import lol.kv3rk.draft_predict.DefaultPipeline.Component.ChampionIdDB;
import lol.kv3rk.draft_predict.DefaultPipeline.Service.GatherMatchInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EndpointService {

    private final GatherMatchInfo gatherMatchInfo;
    private final ChampionIdDB championIdDB;

    @Autowired
    public EndpointService(
            GatherMatchInfo gatherMatchInfo,
            ChampionIdDB championIdDB
    ) {

        this.gatherMatchInfo = gatherMatchInfo;

        this.championIdDB = championIdDB;
    }

    public void go() throws InterruptedException {

        championIdDB.populateChampionAndIdsDB();
        gatherMatchInfo.getEUWMatchInfo();
        gatherMatchInfo.getNAMatchInfo();
        gatherMatchInfo.getKRMatchInfo();
        gatherMatchInfo.getEUNEMatchInfo();

    }

}
