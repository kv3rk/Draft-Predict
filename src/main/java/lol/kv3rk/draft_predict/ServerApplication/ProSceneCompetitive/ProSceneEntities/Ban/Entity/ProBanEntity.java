package lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Ban.Entity;

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
@Table(name = "pro_ban")
@Entity
@Builder
public class ProBanEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "teamId", referencedColumnName = "id")
    private ProTeamEntity teamId;

    @Column(nullable = false)
    private String ban1;

    @Column(nullable = false)
    private String ban2;

    @Column(nullable = false)
    private String ban3;

    @Column(nullable = false)
    private String ban4;

    @Column(nullable = false)
    private String ban5;
}
