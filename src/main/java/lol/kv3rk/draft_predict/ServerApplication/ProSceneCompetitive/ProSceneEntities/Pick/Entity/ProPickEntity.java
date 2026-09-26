package lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Pick.Entity;

import jakarta.persistence.*;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Team.Entity.ProTeamEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pro_pick")
@Entity
@Builder
public class ProPickEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "teamId", referencedColumnName = "id")
    private ProTeamEntity teamId;

    @Column(nullable = false)
    private String champion;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false, name = "player_name")
    private String playerName;

    @Column(nullable = false, name = "player_id")
    private String playerId;

    @Column(nullable = false, name = "pick_order")
    private Integer pickOrder;

    @Column(nullable = false, name = "gold_diff_at_15")
    private Integer goldDiffAt15;

    @Column(nullable = false, name = "xp_diff_at_15")
    private Integer xpDiffAt15;

    @Column(nullable = false, name = "cs_diff_at_15")
    private Integer csDiffAt15;
}
