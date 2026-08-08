package lol.kv3rk.draft_predict.RankedSoloQ.RankedDbRequests.DTO;

import java.util.Optional;

public interface ChampionPresence {

    String getChampion();

    Optional<Double> getPresence();
}
