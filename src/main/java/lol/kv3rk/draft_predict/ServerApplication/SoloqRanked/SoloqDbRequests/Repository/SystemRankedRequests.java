package lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.Repository;

import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.DTO.Champion;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqEntities.Matches.Entity.MatchesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Query(
            nativeQuery = true,
            value = """
                    select
                    	p.champion as champion
                    from
                    	participants p
                    group by
                    	champion
                    order by
                    	champion;
                    """
    )
    List<Champion> getChampionList();

    @Query(
            nativeQuery = true,
            value = """
                    select
                        COUNT(m.match_id)
                    from matches as m
                    """
    )
    Optional<Long> countMatches();

    @Query(
            nativeQuery = true,
            value = """
                    select actual_patch();
                    """
    )
    Optional<String> actualPatch();

    @Query(
            nativeQuery = true,
            value = """
                    select m.match_date from matches m
                    group by m.match_date
                    order by m.match_date desc
                    limit 1
                    """
    )
    Optional<LocalDate> getDateOfLastMatch();

    @Query(
            nativeQuery = true,
            value = """
                    select
                    	m.patch
                    from
                    	matches m
                    group by
                    	patch
                    order by
                    	patch;
                    """
    )
    List<String> getPatchList();
}