package lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Match.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pro_match")
@Entity
@Builder
public class ProMatchEntity {
    @Id
    @Column(name = "game_id")
    private String gameId;

    @Column(nullable = false)
    private String league;

    @Column(nullable = false, name = "match_date")
    private LocalDate date;

    @Column(nullable = false)
    private String patch;
}
