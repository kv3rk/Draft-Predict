package lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO;

import java.util.Optional;

public interface ChampionFlexibility {

    String getChampion();

    Optional<Double> getTop();

    Optional<Double> getJungle();

    Optional<Double> getMiddle();

    Optional<Double> getBottom();

    Optional<Double> getUtility();
}
