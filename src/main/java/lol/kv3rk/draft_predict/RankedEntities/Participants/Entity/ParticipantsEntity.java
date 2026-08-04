package lol.kv3rk.draft_predict.RankedEntities.Participants.Entity;

import jakarta.persistence.*;
import lol.kv3rk.draft_predict.ClientApplication.DTO.TopPerformingChampionsDTO;
import lol.kv3rk.draft_predict.RankedEntities.Matches.Entity.MatchesEntity;
import lombok.*;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SqlResultSetMapping(
        name = "TopPerformingChampionsMapping",
        classes = @ConstructorResult(
                targetClass = TopPerformingChampionsDTO.class,
                columns = {
                        @ColumnResult(name = "champion", type = String.class),
                        @ColumnResult(name = "pick_rate", type = Double.class),
                        @ColumnResult(name = "win_rate", type = Double.class)
                }
        )
)
@NamedNativeQuery(
        name = "getTopPerformingChampions",
        query = """
                WITH total_matches AS (
                                        SELECT COUNT(*) AS total
                                        FROM matches
                                    )
                
                                    SELECT
                                        p.champion,
                                        COUNT(*) * 100.0 / tm.total AS pick_rate,
                                        AVG(CASE WHEN p.win THEN 1.0 ELSE 0.0 END) * 100 AS win_rate
                                    FROM participants p
                                    CROSS JOIN total_matches tm
                                    GROUP BY p.champion, tm.total
                                    ORDER BY pick_rate DESC
                                    LIMIT 5;
                """,
        resultSetMapping = "TopPerformingChampionsMapping"
)
@Entity
@Table(name = "participants")
@Builder
public class ParticipantsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "matchId", referencedColumnName = "matchId", nullable = false)
    private MatchesEntity matchId;

    @Column(nullable = false)
    private String champion;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false, name = "team_id")
    private int teamId;

    @Column(nullable = false)
    private boolean win;
}
