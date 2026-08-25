package lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqEntities.Participants.Repository;

import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqEntities.Participants.Entity.ParticipantsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ParticipantsRepository extends JpaRepository<ParticipantsEntity, UUID> {
}
