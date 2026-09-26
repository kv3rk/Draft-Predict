package lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Pick.Repository;

import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Pick.Entity.ProPickEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProPickRepository extends JpaRepository<ProPickEntity, UUID> {
}
