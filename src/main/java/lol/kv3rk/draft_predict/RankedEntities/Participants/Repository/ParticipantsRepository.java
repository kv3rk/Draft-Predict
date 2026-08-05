package lol.kv3rk.draft_predict.RankedEntities.Participants.Repository;

import lol.kv3rk.draft_predict.ClientApplication.DTO.TopPerformingChampions;
import lol.kv3rk.draft_predict.RankedEntities.Participants.Entity.ParticipantsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipantsRepository extends JpaRepository<ParticipantsEntity, UUID> {

    @Query(
            nativeQuery = true,
            value = """
                                        WITH total_matches AS (
                                            SELECT COUNT(*) AS total
                                            FROM matches
                                        )
                                        SELECT
                                            p.champion as champion,
                                            COUNT(*) * 100.0 / tm.total AS pick_rate,
                                            AVG(CASE WHEN p.win THEN 1.0 ELSE 0.0 END) * 100 AS win_rate
                                        FROM participants p
                                        CROSS JOIN total_matches tm
                                        GROUP BY champion, tm.total
                                        ORDER BY pick_rate DESC
                                        LIMIT 5;
                    """
    )
    List<TopPerformingChampions> getTopPerformingChampionsByPickRate();

}
