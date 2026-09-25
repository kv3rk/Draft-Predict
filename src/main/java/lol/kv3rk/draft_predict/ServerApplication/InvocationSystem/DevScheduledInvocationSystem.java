package lol.kv3rk.draft_predict.ServerApplication.InvocationSystem;

import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.Service.SaveProMatchData;
import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.SoloqGatherInfo.Component.ChampionIdDB;
import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.SoloqGatherInfo.Service.GatherMatchInfo;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.Service.SoloQDbRequestsService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@EnableScheduling
@Component
@Profile("dev")
public class DevScheduledInvocationSystem {

    private final GatherMatchInfo gatherMatchInfo;
    private final ChampionIdDB championIdDB;
    private final SoloQDbRequestsService soloQDbRequestsService;
    private final SaveProMatchData saveProMatchData;

    public DevScheduledInvocationSystem(GatherMatchInfo gatherMatchInfo,
                                        ChampionIdDB championIdDB,
                                        SoloQDbRequestsService soloQDbRequestsService,
                                        SaveProMatchData saveProMatchData
    ) {

        this.gatherMatchInfo = gatherMatchInfo;
        this.championIdDB = championIdDB;
        this.soloQDbRequestsService = soloQDbRequestsService;
        this.saveProMatchData = saveProMatchData;
    }

    @Scheduled(initialDelay = 1, fixedDelay = Long.MAX_VALUE)
    public void everyDayRoutine() throws InterruptedException, IOException {

        saveProMatchData.saveData();
//
//        //------------ Initial EUW refresh ---------------
//        soloQDbRequestsService.refreshMaterializedViewRankedFlexibility();
//        championIdDB.populateChampionAndIdsDB();
//        gatherMatchInfo.getEUWMatchInfo();
//
//        //------------  After EUW refresh ---------------
//        soloQDbRequestsService.refreshMaterializedViewRankedFlexibility();
//        gatherMatchInfo.getNAMatchInfo();
//
//        //------------  After NA refresh ---------------
//        soloQDbRequestsService.refreshMaterializedViewRankedFlexibility();
//        gatherMatchInfo.getKRMatchInfo();
//
//        //------------  After KR refresh ---------------
//        soloQDbRequestsService.refreshMaterializedViewRankedFlexibility();
//        gatherMatchInfo.getEUNEMatchInfo();
    }
}