package lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneDbRequests.Repository;

import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneDbRequests.DTO.ProChampion;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Pick.Entity.ProPickEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProPickRateRequests extends JpaRepository<ProPickEntity, UUID> {

    @Query(
            nativeQuery = true,
            value = """
                    select
                    	pp.champion as champion,
                    	pp.position as position,
                    	count(pp.champion) as count
                    from pro_pick pp
                    group by champion, position
                    order by count desc;
                    """
    )
    List<ProChampion> getProChampionPickRate();
}
