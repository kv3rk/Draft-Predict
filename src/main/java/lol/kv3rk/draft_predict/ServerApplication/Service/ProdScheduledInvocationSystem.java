package lol.kv3rk.draft_predict.ServerApplication.Service;

import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.Component.ChampionIdDB;
import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.Service.GatherMatchInfo;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
@Profile("prod")
public class ProdScheduledInvocationSystem {

    private final GatherMatchInfo gatherMatchInfo;
    private final ChampionIdDB championIdDB;

    public ProdScheduledInvocationSystem(GatherMatchInfo gatherMatchInfo,
                                         ChampionIdDB championIdDB) {
        this.gatherMatchInfo = gatherMatchInfo;
        this.championIdDB = championIdDB;
    }

    @Scheduled(cron = "0 1 0 1/1 * *", zone = "UTC")
    public void everyDayRoutine() throws InterruptedException {

        championIdDB.populateChampionAndIdsDB();
        gatherMatchInfo.getEUWMatchInfo();
        gatherMatchInfo.getNAMatchInfo();
        gatherMatchInfo.getKRMatchInfo();
        gatherMatchInfo.getEUNEMatchInfo();

    }
}