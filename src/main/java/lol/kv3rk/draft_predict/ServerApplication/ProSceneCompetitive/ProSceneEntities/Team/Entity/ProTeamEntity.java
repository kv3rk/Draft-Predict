package lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Team.Entity;

import jakarta.persistence.*;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Match.Entity.ProMatchEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pro_team")
@Entity
@Builder
public class ProTeamEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "game_id", referencedColumnName = "game_id")
    private ProMatchEntity gameId;

    @Column(nullable = false)
    private String league;

    @Column(nullable = false, name = "team_name")
    private String teamName;

    @Column(nullable = false)
    private String side;

    @Column(nullable = false, name = "first_pick")
    private boolean firstPick;

    @Column(nullable = false)
    private boolean result;

}
