package lol.kv3rk.draft_predict.ServerApplication.InvocationSystem;

import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.RankedGatherInfo.Component.ChampionIdDB;
import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.RankedGatherInfo.Service.GatherMatchInfo;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository.SystemRankedRequests;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.Service.RankedSoloQService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@EnableScheduling
@Component
@Profile("prod")
public class ProdScheduledInvocationSystem {

    private final GatherMatchInfo gatherMatchInfo;
    private final ChampionIdDB championIdDB;
    private final RankedSoloQService rankedSoloQService;

    public ProdScheduledInvocationSystem(GatherMatchInfo gatherMatchInfo,
                                         ChampionIdDB championIdDB,
                                         RankedSoloQService rankedSoloQService) {
        this.gatherMatchInfo = gatherMatchInfo;
        this.championIdDB = championIdDB;
        this.rankedSoloQService = rankedSoloQService;
    }

    @Scheduled(cron = "0 1 0 1/1 * *", zone = "UTC")
    public void everyDayRoutine() throws InterruptedException {

        //------------ Initial EUW refresh ---------------
        rankedSoloQService.refreshMaterializedViewRankedFlexibility();
        championIdDB.populateChampionAndIdsDB();
        gatherMatchInfo.getEUWMatchInfo();

        //------------  After EUW refresh ---------------
        rankedSoloQService.refreshMaterializedViewRankedFlexibility();
        gatherMatchInfo.getNAMatchInfo();

        //------------  After NA refresh ---------------
        rankedSoloQService.refreshMaterializedViewRankedFlexibility();
        gatherMatchInfo.getKRMatchInfo();

        //------------  After KR refresh ---------------
        rankedSoloQService.refreshMaterializedViewRankedFlexibility();
        gatherMatchInfo.getEUNEMatchInfo();
    }
}