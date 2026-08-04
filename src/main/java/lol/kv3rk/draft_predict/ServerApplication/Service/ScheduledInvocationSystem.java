package lol.kv3rk.draft_predict.ServerApplication.Service;

import lol.kv3rk.draft_predict.DefaultPipeline.Component.ChampionIdDB;
import lol.kv3rk.draft_predict.DefaultPipeline.Service.GatherMatchInfo;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class ScheduledInvocationSystem {

    private final GatherMatchInfo gatherMatchInfo;
    private final ChampionIdDB championIdDB;

    public ScheduledInvocationSystem(GatherMatchInfo gatherMatchInfo,
                                     ChampionIdDB championIdDB) {
        this.gatherMatchInfo = gatherMatchInfo;
        this.championIdDB = championIdDB;
    }

    @Scheduled(cron = "0 1 23 1/1 * *", zone = "Europe/Moscow")
    public void everyDayRoutine() throws InterruptedException {

        championIdDB.populateChampionAndIdsDB();
        gatherMatchInfo.getEUWMatchInfo();
        gatherMatchInfo.getNAMatchInfo();
        gatherMatchInfo.getKRMatchInfo();
        gatherMatchInfo.getEUNEMatchInfo();

    }
}
