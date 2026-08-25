package lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.Repository;

import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqEntities.Bans.Entity.BansEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BanRateRequests extends JpaRepository<BansEntity, UUID> {

    @Query(
            nativeQuery = true,
            value = """
                    with total_matches AS (
                        SELECT COUNT(*) AS total
                        FROM matches as m
                        where m.patch like :patch
                    )
                    SELECT
                        b.champion,
                        COUNT(*) * 100.0 / tm.total AS ban_rate
                    FROM bans b
                    JOIN matches m
                        ON m.match_id = b.match_id
                    CROSS JOIN total_matches tm
                    where m.patch like :patch and b.champion is not null
                    GROUP BY b.champion, tm.total
                    ORDER BY ban_rate DESC
                    LIMIT 10;
                    """
    )
    List<MostBannedChampions> getTop10BannedChampions(
            @Param("patch") String patch
    );

    @Query(
            nativeQuery = true,
            value = """
                    with total_matches AS (
                        SELECT COUNT(*) AS total
                        FROM matches as m
                        where m.patch like :patch
                    )
                    SELECT
                        b.champion,
                        COUNT(*) * 100.0 / tm.total AS ban_rate
                    FROM bans b
                    JOIN matches m
                        ON m.match_id = b.match_id
                    CROSS JOIN total_matches tm
                    where m.patch = patch and b.champion is not null
                    GROUP BY b.champion, tm.total
                    ORDER BY ban_rate DESC
                    LIMIT 5;
                    """
    )
    List<MostBannedChampions> getMostBannedChampionsByActualPatch(
            @Param("patch") String patch
    );
}
