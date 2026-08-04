package lol.kv3rk.draft_predict.RankedEntities.Participants.Repository;

import lol.kv3rk.draft_predict.ClientApplication.DTO.TopPerformingChampionsDTO;
import lol.kv3rk.draft_predict.RankedEntities.Participants.Entity.ParticipantsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ParticipantsRepository extends JpaRepository<ParticipantsEntity, UUID> {

    @Query(
            nativeQuery = true,
            name = "getTopPerformingChampions"
    )
    List<TopPerformingChampionsDTO> getTopPerformingChampions();

}
