package lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneDbRequests.Repository;

import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Match.Entity.ProMatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemProSceneRequests extends JpaRepository<ProMatchEntity, String> {

    @Query(
            nativeQuery = true,
            value = """
                    select
                    	count(pm.game_id)
                    from pro_match pm;
                    """
    )
    Optional<Long> getAmountOfMatches();
}
