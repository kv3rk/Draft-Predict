package lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Matches.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    private String patch;

    @Column(nullable = false)
    private String server;

    @Column(nullable = false)
    private LocalDate matchDate;
}
