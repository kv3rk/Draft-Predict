package lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Participants.Entity;

import jakarta.persistence.*;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Matches.Entity.MatchesEntity;
import lombok.*;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(nullable = false)
    private int xp;

    @Column(nullable = false)
    private int farm;

    @Column(nullable = false)
    private int gold;
}
