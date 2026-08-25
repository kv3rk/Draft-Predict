package lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.Repository;

import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.DTO.*;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqEntities.Matches.Entity.MatchesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChampionFlexibilityRequests extends JpaRepository<MatchesEntity, String> {

    @Query(
            nativeQuery = true,
            value = """
                    select
                        f.champion,
                        case when (f.total_t * 100.0 / f.total_matches) > a.avg_top then round(f.total_t * 100.0 / f.total_matches, 1) end as top,
                        case when (f.total_j * 100.0 / f.total_matches) > a.avg_jungle then round(f.total_j * 100.0 / f.total_matches, 1) end as jungle,
                        case when (f.total_m * 100.0 / f.total_matches) > a.avg_middle then round(f.total_m * 100.0 / f.total_matches, 1) end as middle,
                        case when (f.total_b * 100.0 / f.total_matches) > a.avg_bottom then round(f.total_b * 100.0 / f.total_matches, 1) end as bottom,
                        case when (f.total_u * 100.0 / f.total_matches) > a.avg_utility then round(f.total_u * 100.0 / f.total_matches, 1) end as utility
                    from flex_agg_16 f
                    cross join flex_avg_16 a
                    where f.champion = :name;
                    """
    )
    ChampionFlexibility getChampionFlexibility(
            @Param("name") String name
    );

}
