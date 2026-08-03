package lol.kv3rk.draft_predict.RankedEntities.Matches.Entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matches")
@Builder
public class MatchesEntity {
    @Id
    private String matchId;

    @Column(nullable = false)
    private double patch;

    @Column(nullable = false)
    private String server;
}
