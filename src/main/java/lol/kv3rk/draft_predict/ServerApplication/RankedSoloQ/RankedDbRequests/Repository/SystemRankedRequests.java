package lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository;

import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Matches.Entity.MatchesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemRankedRequests extends JpaRepository<MatchesEntity, String> {

    @Modifying
    @Query(nativeQuery = true, value = "REFRESH MATERIALIZED VIEW flex_stats_16")
    void refreshFlexStats();

    @Modifying
    @Query(nativeQuery = true, value = "REFRESH MATERIALIZED VIEW flex_agg_16")
    void refreshFlexAgg();

    @Modifying
    @Query(nativeQuery = true, value = "REFRESH MATERIALIZED VIEW flex_avg_16")
    void refreshFlexAvg();
}