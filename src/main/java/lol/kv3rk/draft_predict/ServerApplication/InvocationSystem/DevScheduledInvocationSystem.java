package lol.kv3rk.draft_predict.ServerApplication.InvocationSystem;

import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.RankedGatherInfo.Component.ChampionIdDB;
import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.RankedGatherInfo.Service.GatherMatchInfo;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository.SystemRankedRequests;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@EnableScheduling
@Component
@Profile("dev")
public class DevScheduledInvocationSystem {

    private final GatherMatchInfo gatherMatchInfo;
    private final ChampionIdDB championIdDB;
    private final SystemRankedRequests systemRankedRequests;

    public DevScheduledInvocationSystem(GatherMatchInfo gatherMatchInfo,
                                        ChampionIdDB championIdDB,
                                        SystemRankedRequests systemRankedRequests) {

        this.gatherMatchInfo = gatherMatchInfo;
        this.championIdDB = championIdDB;
        this.systemRankedRequests = systemRankedRequests;
    }

    @Scheduled(initialDelay = Long.MAX_VALUE, fixedDelay = Long.MAX_VALUE)
    @Transactional
    public void everyDayRoutine() throws InterruptedException {

        systemRankedRequests.refreshFlexStats();
        systemRankedRequests.refreshFlexAgg();
        systemRankedRequests.refreshFlexAvg();

        championIdDB.populateChampionAndIdsDB();
        gatherMatchInfo.getEUWMatchInfo();
        gatherMatchInfo.getNAMatchInfo();
        gatherMatchInfo.getKRMatchInfo();
        gatherMatchInfo.getEUNEMatchInfo();
    }
}