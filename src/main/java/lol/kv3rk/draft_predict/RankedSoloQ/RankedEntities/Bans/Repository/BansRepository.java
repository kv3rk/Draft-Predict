package lol.kv3rk.draft_predict.RankedSoloQ.RankedEntities.Bans.Repository;

import lol.kv3rk.draft_predict.ClientApplication.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedEntities.Bans.Entity.BansEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BansRepository extends JpaRepository<BansEntity, UUID> {

    @Query(
            nativeQuery = true,
            value = """
                    with total_matches AS (
                        SELECT COUNT(*) AS total
                        FROM matches as m
                        cross join actual_patch() ap(patch)
                        where m.patch = ap.patch
                    )
                    SELECT
                        b.champion,
                        COUNT(*) * 100.0 / tm.total AS ban_rate
                    FROM bans b
                    JOIN matches m
                        ON m.match_id = b.match_id
                    CROSS JOIN total_matches tm
                    cross join actual_patch() ap(patch)
                    WHERE m.patch = ap.patch and b.champion is not null
                    GROUP BY b.champion, tm.total
                    ORDER BY ban_rate DESC
                    LIMIT 5;
                    """
    )
    List<MostBannedChampions> getMostBannedChampions();
}
