package lol.kv3rk.draft_predict.ServerApplication.Service;

import lol.kv3rk.draft_predict.DefaultPipeline.Component.ChampionIdDB;
import lol.kv3rk.draft_predict.DefaultPipeline.Service.GatherMatchInfo;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
@Profile("dev")
public class DevScheduledInvocationSystem {

    private final GatherMatchInfo gatherMatchInfo;
    private final ChampionIdDB championIdDB;

    public DevScheduledInvocationSystem(GatherMatchInfo gatherMatchInfo,
                                         ChampionIdDB championIdDB) {
        this.gatherMatchInfo = gatherMatchInfo;
        this.championIdDB = championIdDB;
    }

    @Scheduled(initialDelay = Long.MAX_VALUE, fixedDelay = Long.MAX_VALUE)
    public void everyDayRoutine() throws InterruptedException {

        championIdDB.populateChampionAndIdsDB();
        gatherMatchInfo.getEUWMatchInfo();
        gatherMatchInfo.getNAMatchInfo();
        gatherMatchInfo.getKRMatchInfo();
        gatherMatchInfo.getEUNEMatchInfo();

    }
}