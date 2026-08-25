package lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqEntities.Bans.Repository;

import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqEntities.Bans.Entity.BansEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BansRepository extends JpaRepository<BansEntity, UUID> {
}
