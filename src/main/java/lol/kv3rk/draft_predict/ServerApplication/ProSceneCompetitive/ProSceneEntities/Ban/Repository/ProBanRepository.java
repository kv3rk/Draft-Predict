package lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Ban.Repository;

import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Ban.Entity.ProBanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProBanRepository extends JpaRepository<ProBanEntity, UUID> {
}
