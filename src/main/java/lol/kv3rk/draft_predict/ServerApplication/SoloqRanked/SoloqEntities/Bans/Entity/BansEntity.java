package lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqEntities.Bans.Entity;

import jakarta.persistence.*;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqEntities.Matches.Entity.MatchesEntity;
import lombok.*;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bans")
@Builder
public class BansEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "matchId", referencedColumnName = "matchId", nullable = false)
    private MatchesEntity matchId;

    private String champion;

    @Column(nullable = false, name = "team_id")
    private int teamId;
}
