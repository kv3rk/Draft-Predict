package lol.kv3rk.draft_predict.RankedEntities.Bans.Repository;

import lol.kv3rk.draft_predict.RankedEntities.Bans.Entity.BansEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BansRepository extends JpaRepository<BansEntity, UUID> {
}
